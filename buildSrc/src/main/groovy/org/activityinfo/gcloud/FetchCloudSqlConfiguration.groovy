package org.activityinfo.gcloud

import groovy.json.JsonSlurper
import org.activityinfo.store.mysql.MySqlServer
import org.bouncycastle.openssl.PEMReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.security.KeyPair
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

/**
 * Connects to the given project id and obtains the IP address of the
 * MySQL database as well as a client certificate that can be used to connect
 * to the database.
 */
class FetchCloudSqlConfiguration extends DefaultTask {

    @Input
    String projectId = "unset"

    @Input
    String instanceId
    
    public String getCertificateName() {
        def name = System.getProperty("user.name") + "-" + InetAddress.getLocalHost().getHostName()
        return name.trim()
                .replaceAll("([0-9])", { (('A' as char) + Integer.parseInt(it[0])) as char })
                .replaceAll("[^A-Za-z._ -]", "")
    }
    
    public File getClientKeyFile() {
        return new File(getProjectDir(), "client-key.pem")
    }

    public File getClientCertFile() {
        return new File(getProjectDir(), "client-cert.pem")
    }

    public File getServerCertFile() {
        return new File(getProjectDir(), "server-ca.pem")
    }

    public File getProjectDir() {
        File cloudSql = new File(project.gradle.gradleUserHomeDir, "cloudsql")
        return new File( cloudSql, projectId)
    }

    public File getKeyStoreFile() {
        return new File(getProjectDir(), "keystore")
    }

    private gcloud(String... args) {

        def command = new ArrayList()
        command.add 'gcloud'
        command.add "--project=${projectId}"
        command.add '--format=json'
        command.addAll args

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        project.exec {
            commandLine command
            standardOutput = outputStream
        }
        return new JsonSlurper().parseText(outputStream.toString());
    }
    
    @TaskAction
    def fetch() {

        def instance = gcloud('sql', 'instances', 'describe', 'activityinfo')

        projectDir.mkdirs()
        serverCertFile.write instance.serverCaCert.cert

        // Create a client certificate if necessary
        if(!clientKeyFile.exists() || !clientCertFile.exists()) {
            clientKeyFile.parentFile.mkdirs()
            clientKeyFile.delete()
            logger.info("Requesting client certificate at ${clientKeyFile.absolutePath}")
            def response = gcloud('sql', 'ssl', 'client-certs', 'create',
                        certificateName,
                        clientKeyFile.absolutePath,
                        '--instance=activityinfo')

            clientCertFile.write response.cert
        }

        // Prepare a key store for our SSL connection
        writeJavaKeyStore()
        
        project.environment.database.name = 'activityinfo'
        project.environment.database.server = new MySqlServer(
                host: instance.ipAddresses[0].ipAddress,
                username: 'root',
                password: 'root',
                keyStore: keyStoreFile)
    }

    /**
     * Write the SSL certificates to a Java Key Store that can be subsequently used
     * by the MySQL JDBC Driver
     */
    def writeJavaKeyStore() {
        
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());


        def ks = KeyStore.getInstance(KeyStore.getDefaultType());
        def password = "notasecret".toCharArray()
        ks.load(null, password)

        def serverCertificate = parseCertificate(serverCertFile.text)
        def clientCertificate = parseCertificate(clientCertFile.text)

        ks.setCertificateEntry("mysqlServerCACert", serverCertificate)
        ks.setCertificateEntry("mysqlClientCertificate", clientCertificate)
        ks.setKeyEntry("mysqlClientKey", parseKey(clientKeyFile.text), "notasecret".toCharArray(), [ clientCertificate ] as Certificate[])
        
        FileOutputStream fos = new FileOutputStream(getKeyStoreFile());
        ks.store(fos, password);
        fos.close();
    }

    private Certificate parseCertificate(String cert) {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
    }
    
    private def parseKey(String key) {
        PEMReader reader = new PEMReader(new StringReader(key))
        def keyPair = reader.readObject() as KeyPair
        return keyPair.getPrivate()
    }
}
