package com.psg.liq.util;

public class GenerateRandomIdentifier {

	/** 
	 * @Description Function generates 8 character alphanumeric ID
	 * @return String
	 **/
	public String generateID() {
		/****	LOCAL VARIABLES	****/
		int intPref = 2;
		int intSuf = 6;
		String strPref = "";
		String strSuf = "";
		String strRid = "";
		GenerateRandomIdentifier classObj = new GenerateRandomIdentifier();

		// Get and display the alphanumeric string
		strPref = classObj.getPrefString(intPref);
		strSuf = classObj.getBodyString(intSuf);
		// System.out.println(strPref + " and " + strSuf);
		strRid = strPref + strSuf;
		
		return strRid;
	}
	
	/** 
	 * @Description Function generates alphanumeric prefix of ID
	 * @param int n
	 * @return String
	 **/
	public String getPrefString(int n) {

		// chose a Character random from this String
		String strAlphaNumericString = "!#$()*+-.0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		
		StringBuilder sb = new StringBuilder(n);	// create StringBuffer of size 'n'

		for (int i = 0; i < n; i++) {

			// generate a random number (index) between 0 to AlphaNumericString variable length
			int intIndex = (int) (strAlphaNumericString.length() * Math.random());

			// append Character at index to sb
			sb.append(strAlphaNumericString.charAt(intIndex));
		}

		return sb.toString();
	}

	/** 
	 * @Description Function generates alphanumeric body of ID
	 * @param int n
	 * @return String
	 **/
	public String getBodyString(int n) {

		// chose a Character random from this String
		String strAlphaNumericString = "!#$()*+-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]_{}";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {

			// generate a random number (index) between 0 to AlphaNumericString variable length
			int intIndex = (int) (strAlphaNumericString.length() * Math.random());

			// append Character at index to sb
			sb.append(strAlphaNumericString.charAt(intIndex));
		}

		return sb.toString();
	}
}