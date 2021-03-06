package lex_syntax;

import java.io.*;
import java.util.*;

/**
 * Created by Srf on 2016/11/3.
 */
public class LexToSyntax {

    private Map<String, Integer> action_map;
    private Map<String, Integer> goto_map;
    ArrayList<String> CFGs;
    ArrayList<Integer> CFG_symbol_count;
    ArrayList<String[]> parse_table;
    ArrayList<Token> tokens;

    Stack<Integer> status_stack;
    Stack<String> symbol_stack;

    int count = 0;

    public LexToSyntax(String cfg_dir, String table_dir, String input_dir){

        action_map = new HashMap<>();
        goto_map = new HashMap<>();
        CFGs = new ArrayList<>();
        CFG_symbol_count = new ArrayList<>();
        parse_table = new ArrayList<>();

        status_stack = new Stack<>();
        symbol_stack = new Stack<>();

        readCFGs(cfg_dir);
        readParseTable(table_dir);

        LexAnalyzer lex = new LexAnalyzer();
        lex.readFile(input_dir);
        lex.lexHandle();
        tokens = lex.getTokens();

    }

    void outputStack(int index){

        FileWriter fw;
        try {

            if(count==0)
                fw = new FileWriter("output2.txt", false);
            else
                fw = new FileWriter("output2.txt", true);
            count++;
            fw.write(count+"\r\n");
            fw.write("status_stack:\t");
            for(int i=0;i<status_stack.size();i++){
                fw.write(status_stack.get(i)+"");
                fw.write(" ");
            }
            fw.write("\r\n");
            fw.write("symbol_stack:\t");
            for(int i=0;i<symbol_stack.size();i++){
                fw.write(symbol_stack.get(i));
                fw.write(" ");
            }
            fw.write("\r\n");
            fw.write("input:\t\t");
            for(int i=index;i<tokens.size();i++){
                fw.write(tokens.get(i).getAttr()+" ");
            }
            fw.write("\r\n");

            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void outputAction(int type){

        FileWriter fw;
        if(type==1){
            try {

                fw = new FileWriter("output2.txt", true);
                fw.write("action:\t\t");
                fw.write("shift");
                fw.write("\r\n");
                fw.write("----------------------------------------");
                fw.write("\r\n");
                fw.flush();
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(type==3){
            try {

                fw = new FileWriter("output2.txt", true);
                fw.write("action:\t\t");
                fw.write("Accept!");
                fw.write("\r\n");
                fw.write("----------------------------------------");
                fw.write("\r\n");
                fw.flush();
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    void outputAction(int type, String CFG){

        FileWriter fw;
        if(type==2){
            try {

                fw = new FileWriter("output2.txt", true);
                fw.write("action:\t\t");
                fw.write("reduce by ");
                fw.write(CFG);
                fw.write("\r\n");
                fw.write("----------------------------------------");
                fw.write("\r\n");
                fw.flush();
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    void outputError(){

        FileWriter fw;
        try {
            fw = new FileWriter("output2.txt", true);
            fw.write("ERROR!");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void syntaxParse(){

        status_stack.push(0);
        int token_index = 0;

        while(true){

            int status = status_stack.peek();
            Token token = tokens.get(token_index);
            String action;
            if(token.getType().equals("id"))
                action = parse_table.get(status) [action_map.get("id")];
            else
                action = parse_table.get(status) [action_map.get(token.getAttr())];

            outputStack(token_index);

            if(action.charAt(0)=='s'){
                status_stack.push(Integer.parseInt(action.substring(1,action.length())));
                symbol_stack.push(token.getAttr());
                token_index++;
                outputAction(1);
            }
            else if(action.charAt(0)=='r'){
                int reduce_index = Integer.parseInt(action.substring(1,action.length()));
                int pop_count = CFG_symbol_count.get(reduce_index-1);
                for(int i=0;i<pop_count;i++){
                    symbol_stack.pop();
                    status_stack.pop();
                }
                status = status_stack.peek();
                String reduce = CFGs.get(reduce_index-1).substring(0,1);
                symbol_stack.push(reduce);
                int next_status = Integer.parseInt(parse_table.get(status) [goto_map.get(reduce)]);
                if(next_status==0){
                    outputError();
                    break;
                }
                status_stack.push(next_status);
                outputAction(2,CFGs.get(reduce_index-1));
            }
            else if(action.equals("acc")){
                outputAction(3);
                break;
            }
            else{
                outputError();
                break;
            }

        }

    }

    void readCFGs(String file_name){

        InputStream is = null;
        try {
            is = new FileInputStream(new File(file_name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner sc = new Scanner(is);

        for(;sc.hasNextLine();){
            String line = sc.nextLine();
            String[] tmp = line.split(",");
            CFGs.add(tmp[0].trim());
            CFG_symbol_count.add(Integer.parseInt(tmp[1].trim()));
        }

    }

    void readParseTable(String file_name){

        InputStream is = null;
        try {
            is = new FileInputStream(new File(file_name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner sc = new Scanner(is);

        String table_head = sc.nextLine();
        String[] pieces = table_head.split("\t");
        int length = pieces.length;
        boolean is_goto = false;
        for(int i=0;i<length;i++){
            if(!is_goto){
                action_map.put(pieces[i], i);
                if(pieces[i].equals("$"))
                    is_goto = true;
            }
            else{
                goto_map.put(pieces[i], i);
            }
        }

        for(;sc.hasNextLine();){
            String table_line = sc.nextLine();
            parse_table.add(table_line.split("\t"));
        }

    }

    public static void main(String[] args){

        LexToSyntax ls = new LexToSyntax("CFGs3.txt","parse_table3.txt","input3.txt");
        ls.syntaxParse();

    }

}
