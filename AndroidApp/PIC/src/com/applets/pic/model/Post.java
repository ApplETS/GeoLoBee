package com.applets.pic.model;

import java.util.Date;

public class Post {
	private String creator;
	private String content;
	//private Image image;
	private Date dateToKill;

	public Post(String creator, String content, Date dateToKill) {
		this.creator = creator;
		this.content = content;
		this.dateToKill = dateToKill;
	}

	public String getCreator() {
		return creator;
	}

	public String getContent() {
		return content;
	}

	public Date getDateToKill() {
		return dateToKill;
	}
	
	

}
