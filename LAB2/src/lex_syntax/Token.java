package lex_syntax;

/**
 * Created by Srf on 2016/10/26.
 */
public class Token {

    String type;
    String attr;

    public Token(String type, String attr){
        this.type = type;
        this.attr = attr;
    }

    public String getType(){
        return this.type;
    }

    public String getAttr(){
        return this.attr;
    }

    public void setAttr(String attr){
        this.attr = attr;
    }

}
