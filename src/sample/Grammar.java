package sample;

import javafx.fxml.FXML;
import jdk.nashorn.internal.runtime.ParserException;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class Grammar {
    static ArrayList<Token> tokenList;
    static ListIterator<Token> iterator;//tokenList的迭代器
    static LinkedList<TreeNode> treeNodeList= new LinkedList<>();//TreeNode链表
    static Token currentToken;//当前的token
    static TreeNode root = null;
    static String GraError="";
    static int a[]=new int[20];
    public static LinkedList<TreeNode> graAnalysis(ArrayList<Token> tokens){
        if(treeNodeList.size()!=0)
            for(int i=treeNodeList.size()-1;i>=0;i--)
                treeNodeList.remove(i);
        tokenList=tokens;
        for(int i=0;i<a.length;i++)
        {
            a[i]=0;
        }
        //tokenList.add(0,new Token(Token.START));
        iterator = tokenList.listIterator();
        TreeNode node = new TreeNode(TreeNode.PROGRAM);
        TreeNode tmp=null;
        treeNodeList.add(node);
        while(iterator.hasNext()){
            tmp=parseStmt();
            node.mNext=tmp;
            node=tmp;
        }
        Show();
        root = treeNodeList.getFirst();
        return treeNodeList;
    }
    private static TreeNode parseStmt() throws ParserException {
        switch (getNextTokenType()) {
            case Token.IF: return parseIfStmt();
            case Token.WHILE: return parseWhileStmt();
            case Token.READ: return parseReadStmt();
            case Token.WRITE: return parseWriteStmt();
            case Token.INT:
            case Token.DOUBLE:
            case Token.STRING:
            case Token.VOID: return parseDeclareStmt();//变量声明+函数声明
            case Token.LBRACE: return parseStmtBlock();
            case Token.ID: return parseAssignStmt();
            case Token.RETURN: return parseReturnStmt();
            default:
                GraError+=("line " + currentToken.lineNo + " : expected token");
                if(iterator.hasNext())  currentToken = iterator.next();
                GraError+="\n";
                return new TreeNode(TreeNode.WRONG);
        }
    }//stmt-block
    private static TreeNode parseReturnStmt() throws ParserException {
        TreeNode node;
        if(a[0]==0)
        {
            node = new TreeNode(TreeNode.RETURN_STMT);
            a[0]++;
        }
        else
        {
            node = new TreeNode(TreeNode.RETURN_STMT,a[0]);
            a[0]++;
        }
        consumeNextToken(Token.RETURN);//消耗一个return
        node.mLeft=parseExp();//exp
        consumeNextToken(Token.SEMI);
        return node;
    }
    private static TreeNode parseIfStmt() throws ParserException {
        TreeNode node;
        if(a[1]==0)
        {
            node = new TreeNode(TreeNode.IF_STMT);
            a[1]++;
        }
        else
        {
            node = new TreeNode(TreeNode.IF_STMT,a[1]);
            a[1]++;
        }
        consumeNextToken(Token.IF);//消耗一个if
        consumeNextToken(Token.LPARENT);//消耗一个左括号
        node.mLeft=parseExp();//exp
        consumeNextToken(Token.RPARENT);
        node.mMiddle=parseStmt();//stmt-block
        if (getNextTokenType() == Token.ELSE) {
            consumeNextToken(Token.ELSE);
            node.mRight=parseStmt();//stmt-block
        }
        return node;
    }
    private static TreeNode parseWhileStmt() throws ParserException {
        TreeNode node;
        if(a[2]==0)
        {
            node = new TreeNode(TreeNode.WHILE_STMT);
            a[2]++;
        }
        else
        {
            node = new TreeNode(TreeNode.WHILE_STMT,a[2]);
            a[2]++;
        }

        consumeNextToken(Token.WHILE);//消耗一个while
        consumeNextToken(Token.LPARENT);//消耗一个左括号
        node.mLeft=parseExp();//exp
        consumeNextToken(Token.RPARENT);
        node.mMiddle=parseStmt();//stmt-block
        return node;
    }
    private static TreeNode parseReadStmt() throws ParserException {
        TreeNode node;
        if(a[3]==0)
        {
            node = new TreeNode(TreeNode.READ_STMT);
            a[3]++;
        }
        else
        {
            node = new TreeNode(TreeNode.READ_STMT,a[3]);
            a[3]++;
        }

        consumeNextToken(Token.READ);
        node.mLeft=variableName();
        consumeNextToken(Token.SEMI);
        return node;
    }
    private static TreeNode parseWriteStmt() throws ParserException {
        TreeNode node;
        if(a[4]==0)
        {
            node = new TreeNode(TreeNode.WRITE_STMT);
            a[4]++;
        }
        else
        {
            node = new TreeNode(TreeNode.WRITE_STMT,a[4]);
            a[4]++;
        }

        consumeNextToken(Token.WRITE);
        node.mLeft=parseExp();
        consumeNextToken(Token.SEMI);
        return node;
    }
    private static TreeNode parseDeclareStmt() throws ParserException{
        if(checkNextTokenType(Token.INT, Token.DOUBLE, Token.STRING, Token.VOID)){//暂时先不分开int double string 和 void吧
            if(iterator.hasNext())  currentToken = iterator.next();
            iterator.next();//foo
            Token tmp = iterator.next();//函数or变量
            iterator.previous();iterator.previous();//iterator.previous();//少移动一个
            if(tmp.tokenNo==Token.LPARENT){//函数声明
                return parseDeclareFunStmt();
            }else {//变量声明
                return parseDeclareVarStmt();
            }
        }else{
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be variable type");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            //throw new ParserException("line " + currentToken.lineNo + " : next token should be variable type");
        }

    }
    private static TreeNode parseDeclareFunStmt() throws ParserException{//函数声明
        TreeNode node;
        if(a[5]==0)
        {
            node = new TreeNode(TreeNode.DECLARE_FUN_STMT);
            a[5]++;
        }
        else
        {
            node = new TreeNode(TreeNode.DECLARE_FUN_STMT,a[5]);
            a[5]++;
        }

        TreeNode varNode;
        if(a[6]==0)
        {
            varNode = new TreeNode(TreeNode.FUN);
            a[6]++;
        }
        else
        {
            varNode = new TreeNode(TreeNode.FUN,a[6]);
            a[6]++;
        }//存储返回值类型和函数名
        //if(checkNextTokenType(Token.INT, Token.DOUBLE, Token.STRING, Token.VOID)){
        if(currentToken.tokenNo==Token.INT||currentToken.tokenNo==Token.DOUBLE||currentToken.tokenNo==Token.STRING||currentToken.tokenNo==Token.VOID){
            int type = currentToken.tokenNo;
            if(type==Token.INT){
                varNode.mDataType=Token.INT;
            }else if(type==Token.DOUBLE){
                varNode.mDataType=Token.DOUBLE;
            }else if(type==Token.STRING){
                varNode.mDataType= Token.STRING;
            }else{
                varNode.mDataType= Token.VOID;
            }
        }else{
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be variable type");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
        }
        if(checkNextTokenType(Token.ID)){
            if(iterator.hasNext())  currentToken = iterator.next();
            varNode.value=currentToken.value;
        }else {
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be ID");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
        }
        node.mLeft=varNode;
        consumeNextToken(Token.LPARENT);
        node.mMiddle=parseParams();//有参数 会新建节点 否则无
        consumeNextToken(Token.RPARENT);
        node.mRight=parseStmtBlock();

        return node;
    }
    private static TreeNode parseParams() throws ParserException{
        if(checkNextTokenType(Token.RPARENT))
        {
            TreeNode node;
            if(a[7]==0)
            {
                node = new TreeNode(TreeNode.PARAMS);
                a[7]++;
            }
            else
            {
                node = new TreeNode(TreeNode.PARAMS,a[7]);
                a[7]++;
            }
            return node;
        }
        else if(checkNextTokenType(Token.INT, Token.DOUBLE, Token.STRING)){

            TreeNode node;
            if(a[8]==0)
            {
                node = new TreeNode(TreeNode.PARAM);
                a[8]++;
            }
            else
            {
                node = new TreeNode(TreeNode.PARAM,a[8]);
                a[8]++;
            }

            TreeNode header = node;
            TreeNode temp= null;

            while(getNextTokenType()!=Token.RPARENT){
                temp=parseParam();
                node.mNext=temp;
                node=temp;
                if(getNextTokenType()==Token.COMMA)
                    consumeNextToken(Token.COMMA);
            }
            return header;

        }else{
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : wrong params");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            //throw new ParserException("line " + currentToken.lineNo + " : wrong params");
        }
    }
    private static TreeNode parseParam() throws ParserException{
        TreeNode node;
        if(a[8]==0)
        {
            node = new TreeNode(TreeNode.PARAM);
            a[8]++;
        }
        else
        {
            node = new TreeNode(TreeNode.PARAM,a[8]);
            a[8]++;
        }

        if(checkNextTokenType(Token.INT, Token.DOUBLE, Token.STRING)){
            if(iterator.hasNext())  currentToken = iterator.next();
            int type = currentToken.tokenNo;
            if(type==Token.INT){
                node.mDataType=Token.INT;
            }else if(type==Token.DOUBLE){
                node.mDataType=Token.DOUBLE;
            }else if(type==Token.STRING){
                node.mDataType= Token.STRING;
            }else{
                node.mDataType= Token.VOID;
            }
        }else{
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be variable type");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            //throw new ParserException("line " + currentToken.lineNo + " : next token should be variable type");
        }

        if(checkNextTokenType(Token.ID)){
            if(iterator.hasNext())  currentToken = iterator.next();
            node.value=currentToken.value;
        }else {
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be ID");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            //throw new ParserException("line " + currentToken.lineNo + " : next token should be ID");
        }

        return node;
    }
    private static TreeNode parseDeclareVarStmt() throws ParserException {
        TreeNode node;
        if(a[9]==0)
        {
            node = new TreeNode(TreeNode.DECLARE_VAR_STMT);
            a[9]++;
        }
        else
        {
            node = new TreeNode(TreeNode.DECLARE_VAR_STMT,a[9]);
            a[9]++;
        }
        TreeNode varNode;
        if(a[10]==0)
        {
            varNode = new TreeNode(TreeNode.VAR);
            a[10]++;
        }
        else
        {
            varNode = new TreeNode(TreeNode.VAR,a[10]);
            a[10]++;
        }

        if(currentToken.tokenNo==Token.INT||currentToken.tokenNo==Token.DOUBLE||currentToken.tokenNo==Token.STRING){
            if(iterator.hasNext())  currentToken = iterator.next();
            int type = currentToken.tokenNo;
            currentToken = iterator.previous();
            if(type==Token.INT){
                varNode.mDataType=Token.INT;
            }else if(type==Token.DOUBLE){
                varNode.mDataType=Token.DOUBLE;
            }else{
                varNode.mDataType= Token.STRING;
            }
        }else{
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be variable type");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            //throw new ParserException("line " + currentToken.lineNo + " : next token should be variable type");
        }
        if(checkNextTokenType(Token.ID)){
            if(iterator.hasNext())  currentToken = iterator.next();
            varNode.value=currentToken.value;
        }else {
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be ID");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            //throw new ParserException("line " + currentToken.lineNo + " : next token should be ID");
        }
        if (getNextTokenType() == Token.ASSIGN) {//单个元素可以声明并赋值
            consumeNextToken(Token.ASSIGN);
            node.mMiddle=parseExp();
        } else if (getNextTokenType() == Token.LBRACKET) {//数组元素要先声明后赋值
            consumeNextToken(Token.LBRACKET);
            varNode.mLeft=parseExp();
            consumeNextToken(Token.RBRACKET);
        }else{}
        consumeNextToken(Token.SEMI);
        node.mLeft=varNode;
        return node;
    }
    private static TreeNode parseStmtBlock() throws ParserException {
        TreeNode node;
        if(a[11]==0)
        {
            node = new TreeNode(TreeNode.BLOCK);
            a[11]++;
        }
        else
        {
            node = new TreeNode(TreeNode.BLOCK,a[11]);
            a[11]++;
        }

        TreeNode header = node;
        TreeNode temp= null;
        consumeNextToken(Token.LBRACE);
        while(getNextTokenType()!=Token.RBRACE){
            temp=parseStmt();
            node.mNext=temp;
            node=temp;
        }
        consumeNextToken(Token.RBRACE);
        return header;
    }
    private static TreeNode parseAssignStmt() throws ParserException {
        TreeNode node;
        if(a[12]==0)
        {
            node = new TreeNode(TreeNode.ASSIGN_STMT);
            a[12]++;
        }
        else
        {
            node = new TreeNode(TreeNode.ASSIGN_STMT,a[12]);
            a[12]++;
        }

        node.mLeft=variableName();
        int type = consumeNextToken(Token.ASSIGN,Token.PLUSEQUAL,Token.MINUSEQUAL,Token.MULTIEQUAL,Token.DIVEQUAL);// a=1;   a+=1;    a++;
//        TreeNode signNode = new TreeNode(type);
//        signNode.mLeft = new TreeNode()
        node.mMiddle=parseExp();
        node.mDataType=type;
        consumeNextToken(Token.SEMI);

        return node;
    }
    private static TreeNode parseExp() throws ParserException {
        TreeNode node;
        if(a[13]==0)
        {
            node = new TreeNode(TreeNode.EXP);
            a[13]++;
        }
        else
        {
            node = new TreeNode(TreeNode.EXP,a[13]);
            a[13]++;
        }
        node.mDataType=Token.LOGIC_EXP;
        TreeNode leftNode = addtiveExp();//exp  or  exp <> exp2

        if(checkNextTokenType(Token.EQ, Token.NEQ, Token.GT, Token.GET, Token.LT, Token.LET)){
            node.mLeft=leftNode;
            node.mMiddle=logicalOp();
            node.mRight=addtiveExp();
            return node;
        }else{
            return leftNode;
        }

    }
    private static TreeNode addtiveExp() throws ParserException {
        TreeNode node;
        if(a[13]==0)
        {
            node = new TreeNode(TreeNode.EXP);
            a[13]++;
        }
        else
        {
            node = new TreeNode(TreeNode.EXP,a[13]);
            a[13]++;
        }
        node.mDataType=Token.ADDTIVE_EXP;
        TreeNode leftNode = term();

        if (checkNextTokenType(Token.PLUS,Token.MINUS)) {
            node.mLeft=leftNode;
            node.mMiddle=addtiveOp();
            node.mRight=addtiveExp();
            return node;
        } else {
            return leftNode;
        }
    }
    private static TreeNode term() throws ParserException {
        TreeNode node;
        if(a[13]==0)
        {
            node = new TreeNode(TreeNode.EXP);
            a[13]++;
        }
        else
        {
            node = new TreeNode(TreeNode.EXP,a[13]);
            a[13]++;
        }
        node.mDataType=Token.TERM_EXP;
        TreeNode leftNode = factor();

        if (checkNextTokenType(Token.MUL, Token.DIV)) {
            node.mLeft=leftNode;
            node.mMiddle=multiplyOp();
            node.mRight=term();
            return node;
        } else {
            return leftNode;
        }
    }
    private static TreeNode factor() throws ParserException {
        if (iterator.hasNext()) {
            TreeNode expNode;
            if(a[14]==0)
            {
                expNode = new TreeNode(TreeNode.FACTOR);
                a[14]++;
            }
            else
            {
                expNode = new TreeNode(TreeNode.FACTOR,a[14]);
                a[14]++;
            }

            switch (getNextTokenType()) {
                case Token.LPARENT://(exp)
                    consumeNextToken(Token.LPARENT);
                    expNode = parseExp();
                    consumeNextToken(Token.RPARENT);
                    break;
                case Token.LITERAL_INT:
                case Token.LITERAL_DOUBLE:
                    expNode.mLeft=literal();
                    break;
                case Token.MINUS://+a
                    expNode.mDataType=Token.MINUS;
                    if(iterator.hasNext())  currentToken = iterator.next();
                    expNode.mLeft=term();
                    break;
                case Token.PLUS://-a
                    expNode.mDataType=Token.PLUS;
                    if(iterator.hasNext())  currentToken = iterator.next();
                    expNode.mLeft=term();
                    break;
                default:// [a]+b
                    //返回的不是expNode
                    return variableName();
            }
            return expNode;
        }
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        GraError+=("line " + currentToken.lineNo + " : next token should be factor");
        GraError+="\n";
        return  new TreeNode(TreeNode.WRONG);
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be factor");
    }
    private static TreeNode literal() throws ParserException {//实际值节点
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.tokenNo;
            TreeNode node;
            if(a[15]==0)
            {
                node = new TreeNode(TreeNode.LITERAL);
                a[15]++;
            }
            else
            {
                node = new TreeNode(TreeNode.LITERAL,a[15]);
                a[15]++;
            }
            node.mDataType=type;
            node.value=currentToken.value;
            if (type == Token.LITERAL_INT || type == Token.LITERAL_DOUBLE|| type == Token.LITERAL_STRING) {
                return node;
            } else {
                // continue execute until throw
            }
        }
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        GraError+=("line " + currentToken.lineNo + " : next token should be literal value");
        GraError+="\n";
        return  new TreeNode(TreeNode.WRONG);
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be literal value");
    }
    private static TreeNode logicalOp() throws ParserException {//== <> >= <= > < 逻辑运算符
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.tokenNo;
            if (type == Token.EQ
                    || type == Token.GET
                    || type == Token.GT
                    || type == Token.LET
                    || type == Token.LT
                    || type == Token.NEQ) {
                TreeNode node;
                if(a[16]==0)
                {
                    node = new TreeNode(TreeNode.OP);
                    a[16]++;
                }
                else
                {
                    node = new TreeNode(TreeNode.OP,a[16]);
                    a[16]++;
                }

                node.mDataType=type;
                return node;
            }
        }
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        GraError+=("line " + currentToken.lineNo + " : next token should be logical operator");
        GraError+="\n";
        return  new TreeNode(TreeNode.WRONG);
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be logical operator");
    }
    private static TreeNode addtiveOp() throws ParserException {//+ -
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.tokenNo;
            if (type == Token.PLUS || type == Token.MINUS) {
                TreeNode node;
                if(a[16]==0)
                {
                    node = new TreeNode(TreeNode.OP);
                    a[16]++;
                }
                else
                {
                    node = new TreeNode(TreeNode.OP,a[16]);
                    a[16]++;
                }
                node.mDataType=type;
                return node;
            }
        }
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        GraError+=("line " + currentToken.lineNo + " : next token should be addtive operator");
        GraError+="\n";
        return  new TreeNode(TreeNode.WRONG);
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be addtive operator");
    }
    private static TreeNode multiplyOp() throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            int type = currentToken.tokenNo;
            if (type == Token.MUL || type == Token.DIV) {
                TreeNode node;
                if(a[16]==0)
                {
                    node = new TreeNode(TreeNode.OP);
                    a[16]++;
                }
                else
                {
                    node = new TreeNode(TreeNode.OP,a[16]);
                    a[16]++;
                }
                node.mDataType=type;
                return node;
            }
        }
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        GraError+=("line " + currentToken.lineNo + " : next token should be multiple operator");
        GraError+="\n";
        return  new TreeNode(TreeNode.WRONG);
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be multiple operator");
    }
    private static TreeNode variableName() throws ParserException {
        TreeNode node;
        if(a[10]==0)
        {
            node = new TreeNode(TreeNode.VAR);
            a[10]++;
        }
        else
        {
            node = new TreeNode(TreeNode.VAR,a[10]);
            a[10]++;
        }
        if (checkNextTokenType(Token.ID)) {
            if(iterator.hasNext())  currentToken = iterator.next();
            node.value=currentToken.value;
        } else {
            while(iterator.hasNext()){
                currentToken = iterator.next();
                if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                    break;
                }
            }
            GraError+=("line " + currentToken.lineNo + " : next token should be ID");
            GraError+="\n";
            return  new TreeNode(TreeNode.WRONG);
            // throw new ParserException("line " + currentToken.lineNo + " : next token should be ID");
        }
        if (getNextTokenType() == Token.LBRACKET) {//a[0]
            consumeNextToken(Token.LBRACKET);
            node.mLeft=parseExp();
            consumeNextToken(Token.RBRACKET);
        }
        //TODO：if   函数调用    a+foo()   foo可以作为factor   《》   如果返回是个string  是语义分析的内容....
        return node;
    }
    private static int getNextTokenType() {
        if (iterator.hasNext()) {
            int type = iterator.next().tokenNo;
            iterator.previous();
            return type;
        }
        return 0;
    }//获取下一个的tokenNo，如果没有（结束了）返回0
    /*  private static int currentToken.lineNo {
          if (iterator.hasNext()) {
              int type = iterator.next().lineNo;
              iterator.previous();
              return type;
          }
          return 0;
      }*///获取下一个的lineNo，如果没有（结束了）返回0
    private static int consumeNextToken(int ...type) throws ParserException {
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            /*if (currentToken.tokenNo == type) {
                return;
            }*/
            for (int i : type) {
                if(currentToken.tokenNo==i)
                    return currentToken.tokenNo;
            }
        }
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        GraError+=("line " + currentToken.lineNo + " : next token should be -> operator");
        GraError+="\n";
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be -> operator");
        return -1;
    }
    private static void consumeNextToken(int type) throws ParserException {
        String t="";
        int line=0;
        if (iterator.hasNext()) {
            currentToken = iterator.next();
            line=currentToken.lineNo;
            if (currentToken.tokenNo == type) {
                return;
            }
        }
        //consumeNextToken();
        while(iterator.hasNext()){
            currentToken = iterator.next();
            if(currentToken.tokenNo==Token.RBRACE||currentToken.tokenNo==Token.RPARENT||currentToken.tokenNo==Token.SEMI) {
                break;
            }
        }
        if(type==19) t="SEMI";
        else if(type==26) t="RBRACKET";
        else if(type==18) t="RPARENT";
        else t=type+"";
        GraError+=("line " + line + " : next token should be -> " + t);
        GraError+="\n";
        //throw new ParserException("line " + currentToken.lineNo + " : next token should be -> " + new Token(type, 0));
    }//消耗一个tokenNO期望的token
    private static boolean checkNextTokenType(int ... type) {
        if (iterator.hasNext()) {
            int nextType = iterator.next().tokenNo;
            iterator.previous();
            for (int each : type) {
                if (nextType == each) {
                    return true;
                }
            }
        }
        return false;
    }
    private static void Show() {
        System.out.println("treeNodeList:");
        for(TreeNode t : treeNodeList){
            System.out.println(t.toString());
        }
    }

}
