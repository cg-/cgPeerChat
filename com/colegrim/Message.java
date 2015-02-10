/*
 * Project: Chat
 * Author: Cole Grim (cg-)
 * Created: 2/9/2015
 */

package com.colegrim;

/**
 * A model for a message
 */
public class Message {
    public final int VERSION = 1;
    /**
     * Enums for each type, and a code to encode them with.
     */
    public enum MessageType{
        ChatMessage("CHAT"), ListUpdate("LIST"), LogOff("LOGO"), ServerShutdown("SHUT"), Login("LOGI");

        final String code;

        MessageType(String code){
            this.code = code;
        }
    }

    private MessageType type;
    private String payload;

    // Constructor
    public Message(MessageType type, String payload){
        this.type = type;
        this.payload = payload;
    }

    /**
     * Gets the contents of the message
     * @return Message contents.
     */
    public String getPayload(){
        return this.payload;
    }

    /**
     * Gets the type of the message
     * @return Message type
     */
    public MessageType getType(){
        return this.type;
    }

    /**
     * Generates a Message from a properly formatted String.
     *
     * The String should be formatted as follows:
     * <type code>~<payload>
     *
     * toString will generate a properly formatted String
     *
     * @param s Properly formatted String
     * @return A Message object
     * @throws Exception If an invalid String is provided.
     */
    public static Message fromString(String s) throws Exception{
        String[] tokens = s.split("~");
        MessageType type = null;
        for(MessageType t : MessageType.values()){
            if(t.code.matches(tokens[0])) type = t;
        }
        if(type == null) throw new Exception("Invalid messagestring: " + s);
        String payload = s.substring(5);
        return new Message(type, payload);
    }

    /**
     * Generates a properly formatted String for a Message
     *
     * See fromString for information on formatting.
     *
     * @return A properly formatted String
     */
    @Override
    public String toString(){
        return this.type.code + "~" + this.payload;
    }
}
