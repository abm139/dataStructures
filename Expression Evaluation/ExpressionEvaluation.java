package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	String varName = "";
    	for(int i = 0; i < expr.length(); i++) 
    	{
    		if (Character.isLetter(expr.charAt(i)))
    		{
    			varName += expr.charAt(i);
    		}
    		else if(expr.charAt(i) == '[') 
    		{
    			boolean acontained = false;
    			for(int j = 0; j < arrays.size(); j++)
    	    	{
    	    		if(arrays.get(j).name.equals(varName))
    	    			acontained = true;
    	    	}
    			if(!acontained)
    				arrays.add(new Array(varName));
    			varName = "";
    		}
    		else if(varName != "")
    		{
    			boolean contained = false;
    			for(int j = 0; j < vars.size(); j++)
    	    	{
    	    		if(vars.get(j).name.equals(varName))
    	    			contained = true;
    	    	}
    			if(!contained)
    				vars.add(new Variable(varName));
    			varName = "";
    		}
    		if(i == expr.length() -1 && varName != "") {
    			boolean contained = false;
    			for(int j = 0; j < vars.size(); j++)
    	    	{
    	    		if(vars.get(j).name.equals(varName))
    	    			contained = true;
    	    	}
    			if(!contained)
    				vars.add(new Variable(varName));
    			varName = "";
    		}
    	}
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }

    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	expr = expr.replace(" ", "");
    	expr = replaceVars(expr, vars);
    	expr = replaceArrays(expr, arrays);
    	expr = evaluateP(expr);
    	return Float.parseFloat(expr);
    }
    
private static String replaceVars(String expr, ArrayList<Variable> vars) {
	String varName = "";
	for(int i = 0; i < expr.length(); i++) 
	{
		if (Character.isLetter(expr.charAt(i)))
		{
			varName += expr.charAt(i);
		}
		else if(expr.charAt(i) == '[') 
		{
			varName = "";
		}
		else if(varName != "")
		{
			float varVal = 0;
			for(int j = 0; j < vars.size(); j++)
			{
				if(vars.get(j).name.equals(varName))
				{
					varVal = vars.get(j).value;
				}	
			}
			int digitCount = (varVal + "").length();
			expr = expr.substring(0,i - varName.length()) + varVal + expr.substring(i);
			i -= varName.length() - digitCount + 1;
			varName = "";
		}
		if(i == expr.length() -1 && varName != "") {
			float varVal = 0;
			for(int j = 0; j < vars.size(); j++)
			{
				if(vars.get(j).name.equals(varName))
					varVal = vars.get(j).value;
			}
			expr = expr.substring(0,i - varName.length() + 1) + varVal;
			return expr;
		}	
	}
	return expr;
	
}

/**
 * @param expr
 * @param arrays
 * @return
 */
private static String replaceArrays(String expr, ArrayList<Array> arrays) {

	if(expr.indexOf('[') == -1 || expr == "") 
	{
		expr = evaluateP(expr);
		return expr;
	}
	int bcount = 1;
	int startbIndex = -1;
	int endIndex = -1;
	int startIndex = -1;
	String varName = "";
	for(int i = 0; i < expr.length(); i++)
	{
		if(expr.indexOf('[') == -1)
			break;
		
		if (Character.isLetter(expr.charAt(i)))
		{
			varName += expr.charAt(i);
			if(startIndex == -1)
				startIndex = i;
		}
		else if(expr.charAt(i) == '[')
		{
			if(startbIndex == -1)
				startbIndex = i;
			while(bcount != 0 || startbIndex == i) 
			{
				i++;
				if(expr.charAt(i) == '[')
					bcount += 1;
				else if(expr.charAt(i) == ']') 
					bcount -= 1;
			}
			endIndex = i;
		}
		if(startbIndex != -1 && endIndex != -1)
		{
			String inner = replaceArrays(expr.substring(startbIndex + 1, endIndex), arrays);
			int[] array = new int[69];
			for(int j = 0; j < arrays.size(); j++)
			{
				if(arrays.get(j).name.equals(varName))
					array = arrays.get(j).values;
			}
			expr = expr.substring(0, startIndex) + array[Math.round(Float.parseFloat(inner))] + expr.substring(endIndex + 1);
			varName = "";
			i = -1;
			startbIndex = -1;
			startIndex = -1;
			endIndex = -1;
			bcount = 1;
		}
	}
	expr = replaceArrays(expr, arrays);
	return expr;
}


private static String evaluateP(String expr) {
	if(expr.indexOf('(') == -1 || expr == "") 
	{
		expr = evaluateMD(expr);
		expr = evaluateAS(expr);
		return expr;
	}
	int pcount = 1;
	int startIndex = -1;
	int endIndex = -1;
	for(int i = 0; i < expr.length(); i++)
	{
		if(expr.indexOf('(') == -1)
			break;
		if(expr.charAt(i) == '(')
		{
			if(startIndex == -1)
				startIndex = i;
			while(pcount != 0 || startIndex == i) 
			{
				i++;
				if(expr.charAt(i) == '(')
					pcount += 1;
				else if(expr.charAt(i) == ')') 
					pcount -= 1;
			}
			endIndex = i;
		}
		if(startIndex != -1 && endIndex != -1)
		{
			String ans = evaluateP(expr.substring(startIndex + 1, endIndex));
			expr = expr.substring(0, startIndex) + ans + expr.substring(endIndex + 1);
			i = -1;
			startIndex = -1;
			endIndex = -1;
			pcount = 1;
		}
	}
	expr = evaluateP(expr);
	return expr;
}


private static String evaluateMD(String expr) {
	String firstNum = "";
	String secondNum = "";
	int startIndex = -1;
	int endIndex = -1;
	float ans = 0;
	if(expr.indexOf('*') == -1 && expr.indexOf('/') == -1 || expr == "")
		return expr;
	for(int i = 0; i < expr.length(); i++)
	{
		if(Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.' || (firstNum == "" && expr.charAt(i) == '-')) 
		{
			if(startIndex == -1)
				startIndex = i;
			firstNum += expr.charAt(i);
		}
		else if(firstNum != "")
		{
			if(expr.charAt(i) == '+' || expr.charAt(i) == '-')
			{
				firstNum = "";
				startIndex = -1;
				continue;
			}
			else 
			{
				String action = expr.charAt(i) + "";
				i++;
				while(i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.' || expr.charAt(i) == '-'))
				{
					secondNum += expr.charAt(i);
					i++;
				}
				if(action.equals("*"))
					ans = Float.parseFloat(firstNum) * Float.parseFloat(secondNum);
				else
					ans = Float.parseFloat(firstNum) / Float.parseFloat(secondNum);
				endIndex = i;
				expr = expr.substring(0,startIndex) + ans + expr.substring(endIndex);
				return evaluateMD(expr);
			}
		}
	}
	return expr;
}


private static String evaluateAS(String expr) {
	String firstNum = "";
	String secondNum = "";
	int startIndex = -1;
	int endIndex = -1;
	float ans = 0;
	if(expr.indexOf('+') == -1 && expr.indexOf('-') == -1 || expr == "")
		return expr;
	for(int i = 0; i < expr.length(); i++)
	{
		if(Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.' || (firstNum == "" && expr.charAt(i) == '-')) 
		{
			if(startIndex == -1)
				startIndex = i;
			firstNum += expr.charAt(i);
		}
		else if(firstNum != "")
		{
				String action = expr.charAt(i) + "";
				i++;
				if(expr.charAt(i) == '-')
				{
					if(action.equals("+"))
						action = "-";
					else
						action = "+";
					expr = expr.substring(0,i) + expr.substring(i+1);
				}
				while(i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.'))
				{
					secondNum += expr.charAt(i);
					i++;
				}
				if(action.equals("+"))
					ans = Float.parseFloat(firstNum) + Float.parseFloat(secondNum);
				else
					ans = Float.parseFloat(firstNum) - Float.parseFloat(secondNum);
				endIndex = i;
				expr = expr.substring(0,startIndex) + ans + expr.substring(endIndex);
				return evaluateAS(expr);
		}
	}
	return expr;
}

}