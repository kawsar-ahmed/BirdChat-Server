package com.socket;

import java.io.Serializable;

public class Message implements Serializable{
    
    private static final long serialVersionUID = 1L;
    public String type, sender, content, recipient, dataType;

    public Message(String type, String sender, String content, String recipient){
        this.type = type; 
        this.sender = sender; 
        this.content = content; 
        this.recipient = recipient;
    }
    
    public Message(String type, String sender, String content, String dataType, String recipient){
        this.type = type; 
        this.sender = sender; 
        this.content = content; 
        this.recipient = recipient;
        this.dataType = dataType;
    }
    
    @Override
    public String toString(){
        return "{type='"+type+"', sender='"+sender+"', content='"+content+"', dataType='"+dataType+"', recipient='"+recipient+"'}";
    }
}