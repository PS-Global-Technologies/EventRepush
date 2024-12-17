package com.psg.liq.util;

/****	I/O IMPORTS	****/
import java.io.IOException;
import java.io.StringReader;
/****	SQL IMPORTS	****/
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/****	XML PARSER IMPORT	****/
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
//import org.apache.log4j.LogManager;
/****	//LOGGER IMPORT	****/
//import org.apache.log4j.LOGGER;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/****	SOAP UTIL IMPORT	****/
import com.gib.psg.liq.SoapUtils.WebServiceCallResponse;

public class TransactionAuditUpdate {

	/**** GLOBAL VARIABLES ****/
	private Boolean isTrue = false;
	private String ovd_rid_ost = null;
	private String actualStatus = "RELSD";

	private static final Logger LOGGER = LogManager.getLogger(TransactionAuditUpdate.class);

	/**
	 * @Description Input SOAP request and response data to the Audit Table
	 *              (OUTSTANDING_UPDATE_AUDIT_TABLE). [JOB ID HERE!!!] returns true
	 *              if XML response status is 'FAIL'
	 * @param WebServiceCallResponse webserviceresponse - object containing SOAP API
	 *                               call response data
	 * @param String                 strXmlInput - String of XML input
	 * @param Connection             con
	 * @param ResultSet              resultSet
	 * @return boolean
	 **/
	public boolean pushToReleaseTable(WebServiceCallResponse webserviceresponse, String strXmlInput, Connection con,
			ResultSet resultSet) {
		LOGGER.info("PUSHING DATA TO TIN_BATCH_OVERDRAFTS");
		/**** LOCAL VARIABLES ****/
		boolean responseStatus = false;
		boolean requestDone = false;
		PreparedStatement pstmt = null;

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		String strRID = null;
		try {
			strRID = resultSet.getString(1);
		} catch (SQLException e1) {
			LOGGER.error("***ERROR: " + e1.getMessage());

		}
		String strUpdateResQuery = "update liqcust.TIN_BATCH_OVERDRAFTS set ovd_rid_liq=?,ovd_cde_status=?,ovd_uid_rec_update=?,ovd_tsp_rec_update=? where ovd_rid_overdraft= ?";

		String strStatus = "";

		try {
			/** Insert SOAP request & response data **/
			pstmt = con.prepareStatement(strUpdateResQuery);

			// passes response XMLP string to parser to check status value (SUCCESS OR FAIL)
			try {
				parseXml(webserviceresponse.getResponse());
			} catch (ParserConfigurationException | SAXException | IOException e) {
				LOGGER.error("***ERROR: " + e.getMessage());

			}
			// Sets status to RELSD or the status of the queried record based on XML
			// response
			if (isTrue && ovd_rid_ost != null) {

				pstmt.setString(1, ovd_rid_ost);
				pstmt.setString(2, actualStatus);
				pstmt.setString(3, "LS2BATCH");
				pstmt.setTimestamp(4, timestamp);
				pstmt.setString(5, strRID);
				responseStatus = true;
				requestDone = true;
			} else {
				actualStatus = "FAIL";
				pstmt.setString(1, null);
				pstmt.setString(2, actualStatus);
				pstmt.setString(3, "LS2BATCH");
				pstmt.setTimestamp(4, timestamp);
				pstmt.setString(5, strRID);
			}

			pstmt.execute();
			// con.commit();
			LOGGER.info(strRID + " DATA INSERTED INTO TIN_BATCH_OVERDRAFTS");
			// Write output sent to TIN_OUTSTANDING_RELEASE to out file specified in
			// strOutFilePath
			// out.writeReleaseToOutFile(strOstRID, strOtrRID, dateObject, strStatus,
			// strRID, strOutFilePath, requestDone);

			// After insert to TIN_OUTSTANDING_RELEASE begin insert to TIN_AUDIT_CUSTOM
			pushToAuditTable(webserviceresponse, strXmlInput, con, strRID, requestDone);
			con.commit();
		} catch (SQLException e) {
			LOGGER.info("***ERROR: " + e.getMessage());
		}

		finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					LOGGER.info("***ERROR: " + e.getMessage());
				}
			}
		}

		return responseStatus;
	}

	public void pushToAuditTable(WebServiceCallResponse webserviceresponse, String strXmlInput, Connection con,
			String strRID, boolean requestDone) {
		LOGGER.info("PUSHING DATA TO TIN_AUDIT_CUSTOM ");
		/**** LOCAL VARIABLES ****/
		LOGGER.info("jobId: "+ LiqAPILoadProperties.getProperties().getProperty("jobid"));
		final int intJobId = Integer.parseInt(LiqAPILoadProperties.getProperties().getProperty("jobid"));
		LOGGER.info("jobType: "+ LiqAPILoadProperties.getProperties().getProperty("jobtype"));

		final String strJobType = LiqAPILoadProperties.getProperties().getProperty("jobtype");
		LOGGER.info("ownerType: "+ LiqAPILoadProperties.getProperties().getProperty("ownertype"));
		final String strOwnerType = LiqAPILoadProperties.getProperties().getProperty("ownertype");
		PreparedStatement pstmt = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		GenerateRandomIdentifier uniqueIdGenerator = new GenerateRandomIdentifier();
		String strInsertResQuery = "INSERT INTO liqcust.TIN_AUDIT_CUSTOM  (AUD_RID_AUDIT, AUD_CDE_JOB_ID, AUD_CDE_JOB_TYPE, AUD_CDE_OWNER_TYPE, AUD_RID_OWNER_ID, AUD_TXT_REQUEST, AUD_TXT_RESPONSE, AUD_CDE_STATUS, AUD_UID_REC_CREATE, AUD_UID_REC_UPDATE, AUD_TSP_REC_CREATE, AUD_TSP_REC_UPDATE) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			/** Insert SOAP request & response data **/
			pstmt = con.prepareStatement(strInsertResQuery);
			pstmt.setString(1, uniqueIdGenerator.generateID());
			pstmt.setInt(2, intJobId);
			pstmt.setString(3, strJobType);
			pstmt.setString(4, strOwnerType);
			pstmt.setString(5, strRID);
			pstmt.setString(6, strXmlInput);
			pstmt.setString(7, webserviceresponse.getResponse());
			// Sets status to SUCCESS or FAIL based on XML response
			if (requestDone) {
				pstmt.setString(8, "SUCCESS");
			} else {
				pstmt.setString(8, "FAIL");

			}
			pstmt.setString(9, "LS2BATCH");
			pstmt.setString(10, "LS2BATCH");
			pstmt.setTimestamp(11, timestamp);
			pstmt.setTimestamp(12, timestamp);
			pstmt.execute();
			LOGGER.info(strRID + " DATA INSERTED INTO TIN_AUDIT_CUSTOM\n");
		} catch (SQLException e) {
			LOGGER.info("***ERROR: " + e.getMessage());
		}

		finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					LOGGER.info("***ERROR: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Purpose: To parse through the XML string and extract the attribute value.
	 * 
	 * @param strOutput
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void parseXml(String strOutput) throws ParserConfigurationException, SAXException, IOException {

		/*
		 * SAXParserFactory is a factory API that enables applications to configure and
		 * obtain a SAX based parser to parse XML documents.
		 */
		SAXParserFactory factory = SAXParserFactory.newInstance();

		// Creating a new instance of a SAXParser using the currently configured factory
		// parameters.
		SAXParser saxParser = factory.newSAXParser();

		// DefaultHandler is Default base class for SAX2 event handlers.
		DefaultHandler handler = new DefaultHandler() {

			// Receive notification of the start of an element. parser starts parsing a
			// element
			// inside the document
			@Override
			public void startElement(String strUri, String strLocalName, String strQName, Attributes attributes)
					throws SAXException {

				if (strQName.equalsIgnoreCase("response")) {
					String strData = attributes.getValue("success");
					if (strData.compareToIgnoreCase("true") == 0) {
						isTrue = true;

					} else {
						isTrue = false;
					}
				}
				if (strQName.equalsIgnoreCase("OutstandingTransactionAsReturnValue")) {
					String rid = attributes.getValue("id");
					if (rid != null && rid.length() <= 9) {
						ovd_rid_ost = rid;
						String status = attributes.getValue("status");
						if (status != null && !status.equalsIgnoreCase("Released"))
							actualStatus = "POSTD";

					}

				}

				/*
				 * if (strQName.equalsIgnoreCase("status")) { String status=
				 * attributes.getValue("status"); if (status!=null &&
				 * !status.equalsIgnoreCase("Released")) actualStatus="POSTD";
				 * 
				 * }
				 */
			}

		};
		saxParser.parse(new InputSource(new StringReader(strOutput)), handler);
	}

}