package com.belladati.sdk.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.belladati.sdk.BellaDati;
import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.auth.AuthorizationException;
import com.belladati.sdk.exception.auth.AuthorizationException.Reason;
import com.belladati.sdk.exception.auth.InvalidTimestampException;
import com.belladati.sdk.exception.server.InternalErrorException;
import com.belladati.sdk.exception.server.NotFoundException;
import com.belladati.sdk.exception.server.ServerResponseException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;

/**
 * Verifies that various exception cases are treated correctly.
 * 
 * @author Chris Hennigfeld
 */
@Test
public class ExceptionTest extends SDKTest {

	private final String uri = "some/uri";

	/** server is not available */
	@Test(expectedExceptions = ConnectionException.class)
	public void connectionFailed() throws Exception {
		server.stop();
		service.client.get(uri, service.tokenHolder);
	}

	/** internal server error */
	@Test(expectedExceptions = InternalErrorException.class)
	public void internalError() {
		registerError(500, "");
		service.client.get(uri, service.tokenHolder);
	}

	/** consumer key was empty or missing */
	public void consumerKeyMissing() {
		registerOAuthProblem(400, "missing_consumer");
		assertAuthException(Reason.CONSUMER_KEY_UNKNOWN);
	}

	/** server didn't recognize consumer key */
	public void consumerKeyUnknown() {
		registerOAuthProblem(400, "invalid_consumer");
		assertAuthException(Reason.CONSUMER_KEY_UNKNOWN);
	}

	/** secret didn't match key during OAuth request token request */
	public void secretMismatchOAuth1() {
		server.registerError("/oauth/requestToken", 400, "oauth_problem=invalid_signature");

		try {
			BellaDati.connect(server.getHttpURL()).oAuth("key", "secret");
			fail("Didn't throw authorization exception");
		} catch (AuthorizationException e) {
			assertEquals(e.getReason(), Reason.CONSUMER_SECRET_INVALID);
		}
	}

	/** secret didn't match key during OAuth access token request */
	public void secretMismatchOAuth2() {
		server.registerError("/oauth/accessToken", 400, "oauth_problem=invalid_signature");
		service.tokenHolder.setToken("token", "secret");
		try {
			new OAuthRequestImpl(service.client, service.tokenHolder).requestAccess();
			fail("Didn't throw authorization exception");
		} catch (AuthorizationException e) {
			assertEquals(e.getReason(), Reason.TOKEN_INVALID);
		}
	}

	/** secret didn't match key during xAuth token request */
	public void secretMismatchxAuth() {
		server.registerError("/oauth/accessToken", 400, "oauth_problem=invalid_signature");

		try {
			BellaDati.connect(server.getHttpURL()).xAuth("key", "secret", "username", "password");
			fail("Didn't throw authorization exception");
		} catch (AuthorizationException e) {
			assertEquals(e.getReason(), Reason.CONSUMER_SECRET_INVALID);
		}
	}

	/** secret didn't match key during regular API request */
	public void secretMismatchOther() {
		registerOAuthProblem(400, "invalid_signature");
		service.tokenHolder.setToken("token", "secret");
		assertAuthException(Reason.TOKEN_INVALID);
	}

	/** current actual server response for secret mismatch */
	public void secretMismatchReversed() {
		registerOAuthProblem(403, "signature_invalid");
		service.tokenHolder.setToken("token", "secret");
		assertAuthException(Reason.TOKEN_INVALID);
	}

	/** domain has expired */
	public void domainExpired() {
		registerOAuthProblem(403, "domain_expired");
		assertAuthException(Reason.DOMAIN_EXPIRED);
	}

	/** request or access token is invalid */
	public void tokenInvalid() {
		registerOAuthProblem(400, "invalid_token");
		assertAuthException(Reason.TOKEN_INVALID);
	}

	/** request token is unauthorized */
	public void tokenUnauthorized() {
		registerOAuthProblem(400, "unauthorized_token");
		assertAuthException(Reason.TOKEN_UNAUTHORIZED);
	}

	/** request or access token has expired */
	public void tokenExpired() {
		registerOAuthProblem(400, "token_expired");
		assertAuthException(Reason.TOKEN_EXPIRED);
	}

	/** timestamp doesn't match server time */
	public void timestampRefused() {
		long start = 12345;
		long end = 67890;
		registerError(400, "oauth_problem=timestamp_refused&oauth_acceptable_timestamps=" + start + "-" + end);
		AuthorizationException rawException = assertAuthException(Reason.TIMESTAMP_REFUSED);
		InvalidTimestampException exception = (InvalidTimestampException) rawException;

		assertEquals(exception.getEarliestTimestamp(), start);
		assertEquals(exception.getLatestTimestamp(), end);
	}

	/** xAuth disabled when making an xAuth request */
	public void xAuthDisabled() {
		registerOAuthProblem(403, "x_auth_disabled");
		assertAuthException(Reason.X_AUTH_DISABLED);
	}

	/** BD Mobile disabled when making a request */
	public void xAuthMobileDisabled() {
		registerOAuthProblem(403, "piccolo_not_enabled");
		assertAuthException(Reason.BD_MOBILE_DISABLED);
	}

	/** xAuth username missing */
	public void xAuthCredentialsMissingUsername() {
		registerOAuthProblem(400, "missing_username");
		assertAuthException(Reason.USER_CREDENTIALS_INVALID);
	}

	/** xAuth password missing */
	public void xAuthCredentialsMissingPassword() {
		registerOAuthProblem(400, "missing_password");
		assertAuthException(Reason.USER_CREDENTIALS_INVALID);
	}

	/** xAuth credentials are invalid */
	public void xAuthCredentialsInvalid() {
		registerOAuthProblem(401, "invalid_credentials");
		assertAuthException(Reason.USER_CREDENTIALS_INVALID);
	}

	/** xAuth credentials are invalid (what the server currently returns) */
	public void xAuthCredentialsInvalidLegacy() {
		registerOAuthProblem(401, "permission_denied");
		assertAuthException(Reason.USER_CREDENTIALS_INVALID);
	}

	/** xAuth user is locked */
	public void xAuthUserLocked() {
		registerOAuthProblem(401, "account_locked");
		assertAuthException(Reason.USER_ACCOUNT_LOCKED);
	}

	/** xAuth user is locked (what the server currently returns) */
	public void xAuthUserLockedLegacy() {
		registerOAuthProblem(403, "user_not_active");
		assertAuthException(Reason.USER_ACCOUNT_LOCKED);
	}

	/** xAuth user is not in the domain */
	public void xAuthNotDomainUser() {
		registerOAuthProblem(403, "domain_restricted");
		assertAuthException(Reason.USER_DOMAIN_MISMATCH);
	}

	/** some other authentication problem */
	public void otherOAuthProblem() {
		String problem = "something_else";
		registerOAuthProblem(400, problem);
		AuthorizationException exception = assertAuthException(Reason.OTHER);
		assertTrue(exception.getMessage().contains(problem), "Message didn't contain problem, was " + exception.getMessage());
	}

	/** server returns a not found error */
	public void notFoundError() {
		registerError(404, "not found");
		try {
			service.client.get(uri, service.tokenHolder);
			fail("Didn't throw not found exception");
		} catch (NotFoundException e) {
			assertEquals(e.getUri(), server.getHttpURL() + "/" + uri);
		}
	}

	/** server returns an error that's not an OAuth problem */
	public void otherError() {
		registerError(400, "not an OAuth problem");
		try {
			service.client.get(uri, service.tokenHolder);
			fail("Didn't throw response exception");
		} catch (ServerResponseException e) {
			assertEquals(e.getClass(), UnexpectedResponseException.class);
		}
	}

	/** server returns an error that's not an OAuth problem */
	public void otherErrorCode() {
		registerError(456, "some weird error code");
		try {
			service.client.get(uri, service.tokenHolder);
			fail("Didn't throw response exception");
		} catch (ServerResponseException e) {
			assertEquals(e.getClass(), UnexpectedResponseException.class);
		}
	}

	private void registerError(final int status, final String content) {
		server.registerError("/" + uri, status, content);
	}

	private void registerOAuthProblem(final int status, final String problem) {
		registerError(status, "oauth_problem=" + problem);
	}

	private AuthorizationException assertAuthException(Reason reason) {
		try {
			service.client.get(uri, service.tokenHolder);
			fail("Didn't throw authorization exception");
			return null; // never happens, fail() throws RuntimeException
		} catch (AuthorizationException e) {
			assertEquals(e.getReason(), reason);
			return e;
		}
	}
}
