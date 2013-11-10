<?php
if(isset($_GET["method"])){
    $dbh = createConnection();
    $method = $_GET["method"];
    /*** Start of chat calls ***/
    if($method === "getDefaultChannel") {
        $lat = $_GET["lat"];
        $lon = $_GET["lon"];
        echo getDefaultChannel($dbh, $lat, $lon);
    }
    elseif($method === "getSubChannels") {
        $channel_name = $_GET["channel_name"];
        echo getSubChannels($dbh, $channel_name);
    }
    elseif($method === "createSubChannel") {
        $channel_name = $_GET["channel_name"];
        $sub_channel_name = $_GET["sub_channel_name"];
        print createSubChannel($dbh, $channel_name, $sub_channel_name);
    }
    /*** End of chat calls ***/
    /*** Start of post calls ***/
    elseif($method === "createPost") {
        $creator = $_GET["creator"];
        $content = $_GET["content"];
        if(isset($_GET["picture"])) {
            $picture = $_GET["picture"];
        }
        else {
            $picture = NULL;
        }
        $dateToKill = $_GET["date_to_kill"];
	$institution_id = $_GET["institution_id"];
        print createPost($dbh, $creator, $content, $picture, $dateToKill, $institution_id);
    }
    elseif($method === "getPosts") {
		$channel_name = $_GET["channel_name"];
        print getPosts($dbh, $channel_name);
    }
}


 /************************************************************************
 **************          START OF CHAT API                   *************
 ************************************************************************/

function createConnection() {
    // Connects to your Database
    $dsn = 'mysql:host=localhost;dbname=CHECKINCHAT';
    $username = 'checkinchat';
    $password = 'tX(Dmbs9i&dn^T>lZC6y13|B7ON+PH';
    $options = array(
        PDO::MYSQL_ATTR_INIT_COMMAND => 'SET NAMES utf8',
    );

    $dbh = new PDO($dsn, $username, $password, $options);

    return $dbh;
}


function checkIfInCampus($latMyPosition, $lat1, $lat2, $lonMyPosition, $lon1, $lon2) {
    if($latMyPosition >= min($lat1, $lat2) and $latMyPosition <= max($lat1, $lat2)) {
        if($lonMyPosition >= min($lon1, $lon2) and $lonMyPosition <= max($lon1, $lon2)) {
            return true;
        }
    }

    return false;
}

function getDefaultChannel($dbh, $lat, $lon) {
     $sth = $dbh->query('SELECT * FROM CAMPUS');

     while($info = $sth->fetch(PDO::FETCH_ASSOC)) {
        if(checkIfInCampus($lat, $info['geolatitude1'], $info['geolatitude2'], $lon, $info['geolongitude1'], $info['geolongitude2']) == true) {
            $institutionName = getInstitutionNameById($dbh, $info['institution_id']);
            $subChannels = getSubChannels($dbh, $institutionName);

            $response = json_encode($institutionName);

            if($subChannels !== 'null') {
                $response = $subChannels;
            }

            return $response;
        }
     }

     return "null";
}

//Let you get the institution name by his id
function getInstitutionNameById($dbh, $institution_id){
    $institution_id = mysql_real_escape_string($institution_id);

    if(is_numeric($institution_id)){
        $sth = $dbh->prepare('SELECT name FROM INSTITUTIONS WHERE ID = ?');
        $sth->bindParam(1, $institution_id, PDO::PARAM_INT);
        $sth->execute();

        $result = $sth->fetch(PDO::FETCH_ASSOC);

        return $result['name'];
    }

    return "null";
}

//Lets you get the institution id with the name of it
function getInstitutionIdByName($dbh, $institution_name) {
    $sth = $dbh->prepare('SELECT id FROM INSTITUTIONS WHERE name = ?');
    $sth->bindParam(1, $institution_name, PDO::PARAM_STR);
    $sth->execute();

    $result = $sth->fetch(PDO::FETCH_ASSOC);
    return $result['id'];
}

//Let you create all the subchannels
function getSubChannels($dbh, $channel_name, $institution_id = -1){
    if($institution_id == -1) {
        $channel_name = mysql_real_escape_string($channel_name);
        $institution_id = getInstitutionIdByName($dbh, $channel_name);
    }

    //If we found an institution linked to the name
    if($institution_id != -1) {
        //If the institution id is a number
        if(is_numeric($institution_id)){
            $sth = $dbh->prepare('SELECT name FROM CHANNELS WHERE institution_id = ?');
            $sth->bindParam(1, $institution_id, PDO::PARAM_INT);
            $sth->execute();
            $response = "";

            //We add all the subchannels
            $rows = array();
            while($r = $sth->fetch(PDO::FETCH_ASSOC)) {
                $rows[] = $r;
            }

            return json_encode($rows);
        }
    }
    return "null";
}

//Check if the sub channel the user wants to create already exists
function checkIfSubChannelExists($dbh, $institution_id, $sub_channel_name) {
    $sth = $dbh->prepare('SELECT * FROM CHANNELS WHERE institution_id = ? AND name = ?');
    $sth->bindParam(1, $institution_id, PDO::PARAM_INT);
    $sth->bindParam(2, $sub_channel_name, PDO::PARAM_STR);
    $sth->execute();

    $numberOfRows = $sth->rowCount();

    //If it returns no row, it means that the sub channel doesn't exist
    if($numberOfRows == 0) {
        return false;
    }

    return true;
}

//Let a user create a sub channel for his institution
function createSubChannel($dbh, $channel_name, $sub_channel_name) {
    $channel_name = mysql_real_escape_string($channel_name);
    $sub_channel_name = mysql_real_escape_string($sub_channel_name);
    $institution_id = getInstitutionIdByName($dbh, $channel_name);

    //If we found an institution linked to the name
    if($institution_id !== -1) {
        //If the channel doesn't exist, we create it.
        if(checkIfSubChannelExists($dbh, $institution_id, $sub_channel_name) == false) {
             $sql = "INSERT INTO CHANNELS (institution_id,name) VALUES (:institution_id,:name)";
             $sth = $dbh->prepare($sql);
             $sth->execute(array(':institution_id'=>$institution_id,
                               ':name'=>$sub_channel_name));

        }
    }
}
 /************************************************************************
 **************           END OF CHAT API                    *************
 ************************************************************************/


 /************************************************************************
 **************        START OF BILLBOARD API                *************
 ************************************************************************/

//Creates a post on the billboard with a date to kill it
function createPost($dbh, $creator, $content, $picture, $dateToKill, $institution_id) {
        //If a picture was sent, we transform the picture to make it uploadable in the database
        if($picture != NULL) {
            $pSize = filesize($picture);
            $mysqlPicture = addslashes(fread(fopen($picture, "r"), $pSize));
        }
        //Else we put it to NULL
        else {
            $mysqlPicture = NULL;
        }
        //We add the post
        $sql = "INSERT INTO POSTS (institution_id, creator, content, picture, dateToKill) VALUES (:institution_id,:creator,:content,:picture,:dateToKill)";
                     $sth = $dbh->prepare($sql);
                     $sth->execute(array(':institution_id'=>$institution_id,
										':creator'=>$creator,
                                       ':content'=>$content,
                                       ':picture'=>$mysqlPicture,
                                       ':dateToKill'=>$dateToKill));
}

//Returns the posts that can be shown
function getPosts($dbh, $channel_name) {
	$institution_id = getInstitutionIdByName($dbh, $channel_name);

	if($institution_id !== -1){
		$sth = $dbh->prepare('SELECT creator, content, picture, dateToKill FROM POSTS WHERE dateToKill > NOW() AND institution_id = ?');
		$sth->bindParam(1, $institution_id, PDO::PARAM_INT);
		$sth->execute();
		
		$rows = array();
		while($r = $sth->fetch(PDO::FETCH_ASSOC)) {
		    $rows[] = $r;
		}

		return json_encode($rows);
	}

	return "[]";
}

 /************************************************************************
 **************         END OF BILLBOARD API                 *************
 ************************************************************************/
?>
