import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Created by Srf on 2016/10/25.
 */
public class LexAnalyzer {

    ArrayList<String> line_list;
    ArrayList<String> id_table;
    String reserved_word_table[];

    ArrayList<Token> tokens;

    public LexAnalyzer(){

        line_list = new ArrayList<>();
        id_table = new ArrayList<>();
        reserved_word_table = new String[]{"if","else","for","while","do","break",
                "continue","int","void","char","double","return","main"};

        tokens = new ArrayList<>();

    }

    public void readFile(String file_name){

        InputStream is = null;
        try {
            is = new FileInputStream(new File(file_name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner sc = new Scanner(is);

        for(;sc.hasNextLine();){
            line_list.add(sc.nextLine());
        }

    }

    public void lexHandle(){

        int status;
        //status代表当前词素状态;
        //0为词素开始;1为id或保留字;2位number;3为operator;4为comparison;5为引号内容；6为错误词素;7为各种符号
        String str;
        //str代表当前判断的词素
        String line;
        //line代表当前行字符串

        for(int i=0;i<line_list.size();i++){

            status = 0;
            str = new String();
            line = line_list.get(i);

            for(int j=0;j<line.length();j++){

                char c = line.charAt(j);
                //System.out.println(status);
                switch(characterType(c)){

                    case 1:
                        str = str + c;
                        if(status==0||status==1){
                            status = 1;
                        }
                        else if(status==2){
                            status = 6;
                        }
                        break;

                    case 2:
                        str = str + c;
                        if(status==0||status==2){
                            status = 2;
                        }
                        break;

                    case 3:
                        if(status==0){
                            str = str + '0';
                            str = str + c;
                            status = 2;
                        }
                        else if(status==1){
                            str = str + c;
                            status = 6;
                        }
                        else if(status==2){
                            if(str.length()==1)
                                str = str + c;
                            else{
                                str = str + c;
                                status = 6;
                            }
                        }
                        else if(status==6){
                            str = str + c;
                        }
                        break;

                    case 4:
                        str = str + c;
                        if(status==1){
                            continue;
                        }
                        else{
                            status = 6;
                        }
                        break;

                    case 5:
                        if(status!=0)
                            tokenHandle(str,status);
                        str = new String();
                        str = str + c;
                        tokenHandle(str,3);
                        str = new String();
                        status = 0;
                        break;

                    case 6:
                        if(status!=0)
                            tokenHandle(str,status);
                        str = new String();
                        str = str + c;
                        if(line.charAt(j+1)=='='){
                            str = str + '=';
                            j++;
                        }
                        tokenHandle(str,4);
                        str = new String();
                        status = 0;
                        break;

                    case 7:
                        if(status!=0)
                            tokenHandle(str,status);
                        str = new String();
                        str = str + c;
                        if(line.charAt(j+1)=='='){
                            str = str + '=';
                            j++;
                            tokenHandle(str,4);
                        }
                        else
                            tokenHandle(str,3);
                        str = new String();
                        status = 0;
                        break;

                    case 8:
                        if(status!=0)
                            tokenHandle(str,status);
                        String tmp = "" + c;
                        boolean has_next = false;
                        int k;
                        for(k=j+1;k<line.length();k++){
                            if(line.charAt(k)=='"'){
                                has_next = true;
                                tmp = tmp + line.charAt(k);
                                break;
                            }
                            else
                                tmp = tmp + line.charAt(k);
                        }
                        if(has_next){
                            tokenHandle(tmp,5);
                            j = k;
                        }
                        else{
                            tokenHandle(";",5);
                        }
                        break;

                    case 9:
                        if(status!=0) {
                            tokenHandle(str, status);
                        }
                        if(c!=' '&&c!='\t') {
                            str = new String();
                            str = str + c;
                            tokenHandle(str, 7);
                        }
                        str = new String();
                        status = 0;
                        break;

                    default:
                        str = str + c;
                        status = 6;
                        break;

                }

            }

        }

        tokenMerge();

    }

    //判断字符类型的函数
    private int characterType(char c){

        if(('a'<=c&&c<='z')||('A'<=c&&c<='Z')){
            return 1;
        }
        else if('0'<=c&&c<='9'){
            return 2;
        }
        else if(c=='.'){
            return 3;
        }
        else if(c=='_'){
            return 4;
        }
        else if(c=='+'||c=='-'||c=='*'||c=='/'){
            return 5;
        }
        else if(c=='>'||c=='<'){
            return 6;
        }
        else if(c=='='){
            return 7;
        }
        else if(c=='"'){
            return 8;
        }
        else if(c==';'||c==' '||c=='\t'||c=='('||c==')'||c=='{'||c=='}'){
            return 9;
        }
        else{
            return 10;
        }

    }

    private void tokenHandle(String str, int status){

        switch (status) {

            case 1:
                boolean is_reserved_word = false;
                for(int i=0;i<reserved_word_table.length;i++){
                    if(str.equals(reserved_word_table[i])){
                        is_reserved_word = true;
                        tokens.add(new Token("reserved word",str));
                        break;
                    }
                }
                if(!is_reserved_word){
                    boolean exist = false;
                    for(int i=0;i<id_table.size();i++){
                        if(id_table.get(i).equals(str)){
                            tokens.add(new Token("id",str+" id_table["+i+"]"));
                            exist = true;
                            break;
                        }
                    }
                    if(!exist){
                        tokens.add(new Token("id",str+" id_table["+id_table.size()+"]"));
                        id_table.add(str);
                    }
                }
                break;

            case 2:
                tokens.add(new Token("number",str));
                break;

            case 3:
                tokens.add(new Token("operator",str));
                break;

            case 4:
                tokens.add(new Token("comparison",str));
                break;

            case 5:
                tokens.add(new Token("string",str));
                break;

            case 6:
                tokens.add(new Token("error",str));
                break;

            case 7:
                tokens.add(new Token("other symbol",str));
                break;

        }

    }

    private void tokenMerge(){

        for(int i=1;i<tokens.size()-1;i++){

            if(tokens.get(i).attr.equals("-")&&
                    tokens.get(i+1).type.equals("number")&&
                    (tokens.get(i-1).type.equals("operator")
                            ||tokens.get(i-1).type.equals("comparison"))){
                tokens.remove(i);
                tokens.get(i).setAttr("-"+tokens.get(i).getAttr());
            }

        }

    }

    public void output(){

        FileWriter fw = null;
        try {
            fw = new FileWriter("result.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0;i<tokens.size();i++){
            try {
                fw.write("< ");
                fw.write(tokens.get(i).getType()+" , "+tokens.get(i).getAttr());
                fw.write(" >");
                fw.write("\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args){

        LexAnalyzer lex = new LexAnalyzer();
        lex.readFile("language.txt");
        lex.lexHandle();
        lex.output();

    }

}
