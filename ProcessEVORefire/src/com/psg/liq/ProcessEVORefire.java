package com.psg.liq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.gib.psg.liq.SoapUtils.SoapUtil;
import com.gib.psg.liq.SoapUtils.WebServiceCallResponse;
import com.psg.liq.database.LiqDatabaseConnection;
import com.psg.liq.util.LiqAPILoadProperties;

/********************************************************************************************
 * @author : Rohin
 * @date : 6/12/2021
 * @company : PS Global
 * @version : 1
 * @date : 6-Dec-2021
 *
 * @description : This is the main class, this program is the main class for
 *              batch process to process the OD specific records staged during
 *              Loan IQ EOD.
 *
 ********************************************************************************************/

public class ProcessEVORefire {

	private static final Logger LOGGER = LogManager.getLogger(ProcessEVORefire.class);

	public static void main(String[] args) {
		LOGGER.info("EVO Refire process service started");
		String sql = "";
		String webServiceURL = "";
		LiqAPILoadProperties.loadProperties();
		LOGGER.info("EVO Refire process load properties fetched");
		LiqDatabaseConnection.loadConnection();
		LOGGER.info("EVO Refire process connection fetched");
		sql = LiqAPILoadProperties.getProperties().getProperty("sql");
		LOGGER.info("EVO Refire process sql fetched: " + sql);
		webServiceURL = LiqAPILoadProperties.getProperties().getProperty("apiurl");
		LOGGER.info("EVO Refire process web service URL fetched: " + webServiceURL);

		LOGGER.info("EVO Refire process webServiceURL received from config is: " + webServiceURL);
		PreparedStatement pstmt = null;

		ResultSet rs = null;
		try {

			pstmt = LiqDatabaseConnection.getConnection().prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				LOGGER.info(
						"****************************************************************************************************************\n");
				callNonLoanAPI(rs, LiqDatabaseConnection.getConnection(), webServiceURL);

			}
		} catch (SQLException e) {
			e.printStackTrace();

			LOGGER.error(e.getMessage(), e);
		} finally {

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}

			if (LiqDatabaseConnection.getConnection() != null) {
				try {
					LiqDatabaseConnection.getConnection().close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}

		LOGGER.info("EVO Refire process stopped");
	}

	public static boolean callNonLoanAPI(ResultSet resultSet, Connection con, String url) {
		/**** LOCAL VARIABLES ****/
		String ovd_rid = null;

		boolean responseStatus = false;
		try {
			ovd_rid = resultSet.getString(1);

			LOGGER.info("Processing/Refiring EVO RID: " + ovd_rid);

		} catch (SQLException e) {
			LOGGER.info("***ERROR: " + e.getMessage());
		}
		String strovd_rid = '"' + ovd_rid + '"';

		/**** SOAP API-RELATED VARIABLES ****/
		SoapUtil soapCallObj = new SoapUtil();

		/*
		 * String strXmlInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		 * "<!DOCTYPE CreateNonLoanIncreaseDecrease SYSTEM \"CreateNonLoanIncreaseDecrease.dtd\">\r\n"
		 * + "<CreateNonLoanIncreaseDecrease" + " version=\"1.0\"" +
		 * " requestedAmount= " + strovd_amt + "" + " effectiveDate= " + strovd_date +
		 * " outstandingId=" + strovd_ostid + " alias= " + strovd_ostalias + "" +
		 * "/>\r\n";
		 */

		String strXmlInput = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:api=\"http://api.ws.liq.misys.com/\">\n"
				+ "   <soap:Header/>\n" + "   <soap:Body>\n" + "      <api:executeUpdateEventStatus>\n"
				+ "         <UpdateEventStatus refire=\"Y\" evoRid= " + strovd_rid + " newStatus=\"COMP\"/>\n"
				+ "      </api:executeUpdateEventStatus>\n" + "   </soap:Body>\n" + "</soap:Envelope>";

		if (url == null || url.isBlank()) {
			LOGGER.info("***ERROR: Unable to read web service URL (wsUrl) from application config folder");
			System.exit(8);
		}

		/**
		 * Call soapAPI to execute xmlInput command and save the response object.
		 **/
		LOGGER.info("SENDING SOAP API EVO Refire for EVO RID: " + strovd_rid);

		WebServiceCallResponse webserviceresponse = soapCallObj.callSoapAPI(strXmlInput, url);

		// only for tetsing soap error message

		LOGGER.info(strovd_rid + " XML REQUEST: " + strXmlInput + "\n");

		// CHECK IF THE API's XML RESPONSE IS NULL. IF YES, HANDLE AS FAILED RESPONSE
		if (webserviceresponse.getResponse() != null) {

			LOGGER.info(strovd_rid + " XML RESPONSE: " + webserviceresponse.getResponse() + "\n");
			/**
			 * Call pushToAuditTable input data from webserviceresponse and request to Audit
			 * Table.
			 **/
			LOGGER.info(webserviceresponse.getResponse());
			// responseStatus =
			// transactionAuditUpdate.pushToReleaseTable(webserviceresponse, strXmlInput,
			// con, resultSet);
		} else {
			LOGGER.info("***NOTE: API RETURNED NULL. THIS WILL BE HANDLED AS A FAILED REQUEST.");

		}

		return responseStatus;
	}

}
