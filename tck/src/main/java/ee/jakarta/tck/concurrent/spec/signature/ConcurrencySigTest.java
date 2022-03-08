package ee.jakarta.tck.concurrent.spec.signature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.signaturetest.SigTestEE;
import ee.jakarta.tck.concurrent.framework.signaturetest.SigTestResult;

public class ConcurrencySigTest extends SigTestEE {

	private static final TestLogger log = TestLogger.get(ConcurrencySigTest.class);

	private static final String SIG_FILE_VER_SEP = "_";

	public ConcurrencySigTest() {
		setup();
	}

	/**
	 * Returns a list of strings where each string represents a package name. Each
	 * package name will have it's signature tested by the signature test framework.
	 * 
	 * @return String[] The names of the packages whose signatures should be
	 *         verified.
	 */
	@Override
	protected String[] getPackages(String vehicleName) {
		return new String[] { "jakarta.enterprise.concurrent", "jakarta.enterprise.concurrent.spi" };

	}

	/**
	 * Returns the classpath for the packages we are interested in.
	 */
	protected String getClasspath() {
		String[] classes = new String[] {
				"jakarta.enterprise.concurrent.AbortedException",
				"jakarta.enterprise.util.Nonbinding",
				"jakarta.interceptor.InterceptorBinding"
		};
		
		Set<String> classPaths = new HashSet<String>();
		
		for(String c : classes) {
			try {
				Class<?> clazz = Class.forName(c);
				String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath().toString();
				if(!classPaths.contains(path)) {
					classPaths.add(path);
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Unable to load class " + c + " from application server.");
			}
		}
		
		//Add JDK classes to classpath
		File jimageOutput = new File(testInfo.getJImageDir());
		for(File subFile : jimageOutput.listFiles()) {
			if(subFile.isDirectory()) { //This is a module, we should add it to our classpath.
				classPaths.add(subFile.getAbsolutePath());
			}
		}

		return String.join(":", classPaths);

	}

	protected File writeStreamToTempFile(InputStream inputStream, String tempFilePrefix, String tempFileSuffix)
			throws IOException {
		FileOutputStream outputStream = null;

		try {
			File file = File.createTempFile(tempFilePrefix, tempFileSuffix);
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			while (true) {
				int bytesRead = inputStream.read(buffer);
				if (bytesRead == -1) {
					break;
				}
				outputStream.write(buffer, 0, bytesRead);
			}
			return file;
		}

		finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	protected File writeStreamToSigFile(InputStream inputStream, String packageVersion) throws IOException {
		FileOutputStream outputStream = null;
		String tmpdir = System.getProperty("java.io.tmpdir");
		try {
			File sigfile = new File(
					tmpdir + File.separator + SignatureTests.SIG_FILE_NAME + SIG_FILE_VER_SEP + packageVersion);
			if (sigfile.exists()) {
				sigfile.delete();
				log.info("Existing signature file deleted to create new one");
			}
			if (!sigfile.createNewFile()) {
				log.info("signature file is not created");
			}
			outputStream = new FileOutputStream(sigfile);
			byte[] buffer = new byte[1024];
			while (true) {
				int bytesRead = inputStream.read(buffer);
				if (bytesRead == -1) {
					break;
				}
				outputStream.write(buffer, 0, bytesRead);
			}
			return sigfile;
		}

		finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	/*****
	 * Boilerplate Code
	 * 
	 * /* The following comments are specified in the base class that defines the
	 * signature tests. This is done so the test finders will find the right class
	 * to run. The implementation of these methods is inherited from the super class
	 * which is part of the signature test framework.
	 */

	// NOTE: If the API under test is not part of your testing runtime
	// environment, you may use the property sigTestClasspath to specify
	// where the API under test lives. This should almost never be used.
	// Normally the API under test should be specified in the classpath
	// of the VM running the signature tests. Use either the first
	// comment or the one below it depending on which properties your
	// signature tests need. Please do not use both comments.

	public void signatureTest() throws Fault {
		log.info("ConcurrencySigTest.signatureTest() called");
		SigTestResult results = null;
		String mapFile = null;
		String packageListFile = null;
		String signatureRepositoryDir = null;
		Properties mapFileAsProps = null;
		try {

			InputStream inStreamMapfile = ConcurrencySigTest.class.getClassLoader()
					.getResourceAsStream("ee/jakarta/tck/concurrent/spec/signature/" + SignatureTests.SIG_MAP_NAME);
			File mFile = writeStreamToTempFile(inStreamMapfile, "sig-test", ".map");
			mapFile = mFile.getCanonicalPath();
			log.info("mapFile location is :" + mapFile);

			InputStream inStreamPackageFile = ConcurrencySigTest.class.getClassLoader()
					.getResourceAsStream("ee/jakarta/tck/concurrent/spec/signature/" + SignatureTests.SIG_PKG_NAME);
			File pFile = writeStreamToTempFile(inStreamPackageFile, "sig-test-pkg-list", ".txt");
			packageListFile = pFile.getCanonicalPath();
			log.info("packageFile location is :" + packageListFile);

			mapFileAsProps = getSigTestDriver().loadMapFile(mapFile);
			String packageVersion = mapFileAsProps.getProperty("jakarta.enterprise.concurrent");
			log.info("Package version from mapfile :" + packageVersion);

			InputStream inStreamSigFile = ConcurrencySigTest.class.getClassLoader()
					.getResourceAsStream("ee/jakarta/tck/concurrent/spec/signature/" + SignatureTests.SIG_FILE_NAME
							+ SIG_FILE_VER_SEP + packageVersion);
			File sigFile = writeStreamToSigFile(inStreamSigFile, packageVersion);
			log.info("signature File location is :" + sigFile.getCanonicalPath());
			signatureRepositoryDir = System.getProperty("java.io.tmpdir");

		} catch (IOException ex) {
			log.info("Exception while creating temp files :" + ex);
		}

		String[] packagesUnderTest = getPackages(testInfo.getVehicle());
		String[] classesUnderTest = getClasses(testInfo.getVehicle());
		String optionalPkgToIgnore = testInfo.getOptionalTechPackagesToIgnore();

		// unlisted optional packages are technology packages for those optional
		// technologies (e.g. jsr-88) that might not have been specified by the
		// user.
		// We want to ensure there are no full or partial implementations of an
		// optional technology which were not declared
		ArrayList<String> unlistedTechnologyPkgs = getUnlistedOptionalPackages();

		// If testing with Java 9+, extract the JDK's modules so they can be used
		// on the testcase's classpath.
		Properties sysProps = System.getProperties();
		String version = (String) sysProps.get("java.version");
		if (!version.startsWith("1.")) {
			String jimageDir = testInfo.getJImageDir();
			File f = new File(jimageDir);
			f.mkdirs();

			String javaHome = (String) sysProps.get("java.home");
			log.info("Executing JImage");

			try {
				ProcessBuilder pb = new ProcessBuilder(javaHome + "/bin/jimage", "extract", "--dir=" + jimageDir,
						javaHome + "/lib/modules");
				System.out
						.println(javaHome + "/bin/jimage extract --dir=" + jimageDir + " " + javaHome + "/lib/modules");
				pb.redirectErrorStream(true);
				Process proc = pb.start();
				BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line = null;
				while ((line = out.readLine()) != null) {
					log.info(line);
				}

				int rc = proc.waitFor();
				log.info("JImage RC = " + rc);
				out.close();
			} catch (Exception e) {
				log.info("Exception while executing JImage!  Some tests may fail.");
				e.printStackTrace();
			}
		}
		
		String classpath = getClasspath();

		try {
			results = getSigTestDriver().executeSigTest( //
					packageListFile, // file containing the packages/classes that are to be verified
					mapFile, // sig-test.map file
					signatureRepositoryDir, // directory containing the recorded signatures
					packagesUnderTest, // packages, defined by the test client, that should be tested
					classesUnderTest, // classes, defined by the test client, that should be tested
					classpath, //The location of the API being verified.
					unlistedTechnologyPkgs, // packages that should not exist within the technology under test.
					optionalPkgToIgnore); // packages that should be ignored if found.
			log.info(results.toString());
			if (!results.passed()) {
				log.info("results.passed() returned false");
				throw new Exception();
			}

			// Call verifyJtaJarTest based on some conditions, please check the
			// comment for verifyJtaJarTest.
			if ("standalone".equalsIgnoreCase(testInfo.getVehicle())) {
				if (mapFileAsProps == null || mapFileAsProps.size() == 0) {
					// empty signature file, something unusual
					log.info("ConcurrencySigTest.signatureTest() returning, " + "as signature map file is empty.");
					return;
				}

				boolean isJTASigTest = false;

				// Determine whether the signature map file contains package
				// jakarta.transaction
				String jtaVersion = mapFileAsProps.getProperty("jakarta.transaction");
				if (jtaVersion == null || "".equals(jtaVersion.trim())) {
					log.info("ConcurrencySigTest.signatureTest() returning, "
							+ "as this is neither JTA TCK run, not Java EE CTS run.");
					return;
				}

				log.info("jtaVersion " + jtaVersion);
				// Signature map packaged in JTA TCK will contain a single package
				// jakarta.transaction
				if (mapFileAsProps.size() == 1) {
					isJTASigTest = true;
				}

				if (isJTASigTest || !jtaVersion.startsWith("1.2")) {
					verifyJtaJarTest();
				}
			}
			log.info("$$$ ConcurrencySigTest.signatureTest() returning");
		} catch (Exception e) {
			if (results != null && !results.passed()) {
				throw new Fault("ConcurrencySigTest.signatureTest() failed!, diffs found");
			} else {
				log.info("Unexpected exception " + e.getMessage());
				throw new Fault("ConcurrencySigTest.signatureTest() failed with an unexpected exception", e);
			}
		}
	}
}
