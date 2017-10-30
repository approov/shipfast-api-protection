/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        ApproovHostnameVerifier.kt
 * Original:    Created on 30 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * A Hostname Verifier for verifying TLS connections using Approov.
 *****************************************************************************/

package com.criticalblue.shipfast

import com.criticalblue.attestationlibrary.ApproovAttestation
import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession


/**
 * The ApproovHostnameVerifier class is responsible for verifying TLS sessions
 * for specific host names by validating the X.509 certificate trust chain
 * in the session and comparing the leaf certificate in the TLS session to
 * the leaf certificate in Approov, validated by a token, to mitigate Approov
 * token stealing by Man in the Middle (MitM) attacks.
 */
class ApproovHostnameVerifier(private val delegate: HostnameVerifier) : HostnameVerifier {

    /**
     * Perform host name verification of the given host name and TLS session.
     *
     * @param hostname the host name
     * @param session the TLS session
     * @return true iff host name verification passed
     */
    override fun verify(hostname: String?, session: SSLSession?): Boolean {

        // Ensure we have a host name, TLS session and leaf certificate and
        // return false unless we have all of these
        val hostNameVal = hostname ?: return false
        val sessionVal = session ?: return false
        val certs = sessionVal.peerCertificates.takeIf { it.isNotEmpty() } ?: return false
        val leafCert = certs.first()

        // First, perform the delegate host name verification
        // Second, perform Approov-based host name verification
        return when (delegate.verify(hostNameVal, sessionVal)) {
            true -> {
                try { checkDynamicPinning(hostNameVal, leafCert) }
                catch (e: SSLException) { throw RuntimeException(e) }
            }
            false -> false
        }
    }

    /**
     * Check the given leaf certificate for the given host name matches the one Approov
     * has validated with an Approov token.
     *
     * @param hostname the host name
     * @param leafCert the leaf certificate for the host in the current TLS session
     * @return true iff the given leaf certificate for the current TLS session
     *  matches the version in Approov, ensuring a successful Approov token fetch
     *  has occurred
     */
    private fun checkDynamicPinning(hostname: String, leafCert: Certificate): Boolean {

        // Fetch an Approov token and ensure one was successfully fetched to ensure
        // Approov validates the given host name
        ApproovAttestation.shared().fetchApproovTokenAndWait(hostname)
                .takeIf { it.result == ApproovAttestation.AttestationResult.SUCCESS } ?: return false

        // Retrieve the X.509 DER certificate data for the given host name from Approov
        val approovCertBytes = ApproovAttestation.shared().getCert(hostname) ?: return false

        // Compare the Approov version of the host's certificate with the current
        // session's certificate and only return true if these are valid and equal,
        // otherwise clear the Approov certificates to force re-validation of the
        // host using an Approov token
        return try {
            val approovCert = CertificateFactory.getInstance("X.509")
                    ?.generateCertificate(ByteArrayInputStream(approovCertBytes)) ?: return false
            return when (approovCert == leafCert) {
                true -> true
                false -> { ApproovAttestation.shared().clearCerts(); false }
            }
        }
        catch (e: CertificateException) { ApproovAttestation.shared().clearCerts(); false }
    }
}