<?php
if(isset($_GET["method"])){
    $dbh = createConnection();
    $method = $_GET["method"];
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
        echo createSubChannel($dbh, $channel_name, $sub_channel_name);
    }
}


function createConnection() {
    // Connects to your Database
    $dsn = 'mysql:host=localhost;dbname=PUBLICINSTITUTIONCHAT';
    $username = 'root';
    $password = 'publicchat';
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

            $response = $institutionName;

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
            while($info = $sth->fetch(PDO::FETCH_ASSOC)) {
                //If it's the first subchannel, we assign response to it
                if($response === "") {
                    $response = $info['name'];
                }
                //Else, we add a comma before we add the subchannel
                else {
                    $response = $response . ',' . $channel_name . "." . $info['name'];
                }

            }

            return $response;
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

?>