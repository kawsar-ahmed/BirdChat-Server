/**
 * 
 */
package com.exception;

/**
 * @author Kawsar
 *
 */
public class ExceptionHandler {

	public static String handle(Exception e, boolean throwable) throws Exception{
		if(throwable)
			throw new Exception("Error: "+e.getMessage());
		else
			return "Error: "+e.getMessage();
	}
}
