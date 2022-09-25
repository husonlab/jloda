/*
 * FileUtils.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

import jloda.fx.util.PrintStreamNoClose;
import jloda.util.progress.ProgressPercentage;
import jloda.util.progress.ProgressSilent;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.*;

public class FileUtils {
	private static final Set<File> usedFiles = new HashSet<>();

	/**
	 * given a file name, returns a file with a unique file name.
	 * The file returned has not been seen during the run of this program and doesn't
	 * exist in the file system
	 *
	 * @return file with new and unique name
	 */
	public static File getFileWithNewUniqueName(String name) {
		final String suffix = getFileSuffix(name);
		File result = new File(name);
		int count = 0;
		synchronized (usedFiles) {
			while (usedFiles.contains(result) || result.exists()) {
				result = new File(replaceFileSuffix(name, "-" + (++count) + suffix));

			}
			usedFiles.add(result);
		}
		return result;
	}

	/**
	 * returns name with .suffix removed
	 *
	 * @return name without .suffix
	 */
	public static String getFileBaseName(String name) {
		if (name != null) {
			if (name.endsWith(".gz"))
				name = name.substring(0, name.lastIndexOf(".gz"));
			else if (name.endsWith(".zip"))
				name = name.substring(0, name.lastIndexOf(".zip"));

			var pos = name.lastIndexOf(".");
			if (pos > 0)
				name = name.substring(0, pos);
		}
		return name;
	}

	/**
	 * @return suffix   or null
	 */
	public static String getFileSuffix(String name) {
		if (name == null)
			return null;
		name = getFileNameWithoutPath(name);
		var index = name.lastIndexOf('.');
		if (index > 0)
			return name.substring(index);
		else
			return "";
	}

	/**
	 * returns name with path removed
	 *
	 * @return name without path
	 */
	public static String getFileNameWithoutPath(String name) {
		if (name != null) {
			var pos = name.lastIndexOf(File.separatorChar);
			pos = Math.max(pos, name.lastIndexOf("::") + 1);
			if (pos > 0 && pos < name.length()) {
				name = name.substring(pos + 1);
			}
		}
		return name;
	}

	/**
	 * returns name with path removed
	 *
	 * @return name without path
	 */
	public static String getFileNameWithoutPathOrSuffix(String name) {
		name=getFileNameWithoutPath(getFileNameWithoutPath(name));
		return name==null?"":replaceFileSuffix(name,"");
	}

	/**
	 * get the lines of a files as a list of strings
	 */
	public static ArrayList<String> getLinesFromFile(String file) throws IOException {
		final ArrayList<String> result = new ArrayList<>();
		try (var it = new FileLineIterator(file)) {
			while (it.hasNext()) {
				result.add(it.next());
			}
		}
		return result;
	}

	/**
	 * write lines to file
	 */
	public static void writeLinesToFile(Collection<String> lines, String file, boolean showProgress) throws IOException {
		try (var progress = (showProgress ? new ProgressPercentage("Writing " + file + ":", lines.size()) : new ProgressSilent())) {
			try (var w = getOutputWriterPossiblyZIPorGZIP(file)) {
				for (var line : lines) {
					w.write(line);
					w.write("\n");
					progress.incrementProgress();
				}
			}
		}
	}

	/**
	 * is file an image file?
	 *
	 * @return true, if image file
	 */
	public static boolean isImageFile(File file) {
		if (file.isDirectory())
			return false;
		final String name = file.getName().toLowerCase();
		return name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp") || name.endsWith(".png");
	}

	/**
	 * make a version info file
	 */
	public static void saveVersionInfo(String fileName) throws IOException {
		fileName = replaceFileSuffix(fileName, ".info");
		try (var w = new FileWriter(fileName)) {
			w.write("Created on " + (new Date()) + "\n");
		}
	}

	/**
	 * replace the suffix of a file
	 */
	public static String replaceFileSuffix(String fileName, String newSuffix) {
		if (fileName == null)
			return null;
		else
			return replaceFileSuffix(new File(fileName), newSuffix).getPath();
	}

	/**
	 * replace the suffix of a file, keeping trailing .gz, if present
	 */
	public static String replaceFileSuffixKeepGZ(String fileName, String newSuffix) {
		if (fileName == null)
			return null;
		else {
			var name = replaceFileSuffix(new File(getFileNameWithoutZipOrGZipSuffix(fileName)), newSuffix).getPath();
			if (fileName.endsWith(".gz"))
				return name + ".gz";
			else
				return name;
		}
	}

	/**
	 * replace the suffix of a file
	 */
	public static File replaceFileSuffix(File file, String newSuffix) {
		if (file == null)
			return null;
		else {
			var name = getFileBaseName(file.getName());
			if (newSuffix != null && !name.endsWith(newSuffix))
				name = name + newSuffix;
			return new File(file.getParent(), name);
		}
	}

	public static String getFileNameWithoutZipOrGZipSuffix(String fileName) {
		if (isZIPorGZIPFile(fileName))
			return replaceFileSuffix(fileName, "");
		else
			return fileName;
	}

	public static boolean fileExistsAndIsNonEmpty(String fileName) {
		if (fileName == null)
			return false;
		else return fileExistsAndIsNonEmpty(new File(fileName));
	}

	public static boolean fileExistsAndIsNonEmpty(File file) {
		return file != null && file.exists() && !file.isDirectory() && file.length() > 0;
	}

	public static void checkFileReadableNonEmpty(String... fileNames) throws IOException {
		for (var fileName : fileNames) {
			if(!fileName.equals("stdin")) {
				final File file = new File(fileName);
				if (!file.exists())
					throw new IOException("No such file: " + fileName);
				if (file.length() == 0)
					throw new IOException("File is empty: " + fileName);
				if (!file.canRead())
					throw new IOException("File not readable: " + fileName);
				if (file.getName().endsWith(".gz")) {
					try (InputStream ins = new GZIPInputStream(new FileInputStream(file))) {
						if ((ins.read() == -1))
							throw new IOException("File is empty: " + fileName);
					}
				}
			}
		}
	}

	public static void checkFileWritable(boolean allowOverwrite, String... fileNames) throws IOException {
		for (var fileName : fileNames)
			checkFileWritable(fileName, allowOverwrite);
	}

	public static void checkFileWritable(String fileName, boolean allowOverwrite) throws IOException {
		if (fileName.equalsIgnoreCase("stdout") || fileName.equalsIgnoreCase("stderr")
				|| fileName.equalsIgnoreCase("stdout-gz"))
			return;

		final File file = new File(fileName);
		if (file.exists()) {
			if (!allowOverwrite)
				throw new IOException("File exists: " + fileName);
			else if (!file.delete())
				throw new IOException("Failed to delete existing file: " + fileName);
		}
		if (file.getParent() != null && !file.getParent().isBlank() && !isDirectory(file.getParent()))
			throw new IOException("Containing directory doesn't exist: " + fileName);
		try (var w = getOutputWriterPossiblyZIPorGZIP(fileName)) {
			w.write("");
		} catch (IOException ex) {
			throw new IOException("Can't create file: " + fileName);
		} finally {
			file.delete();
		}
	}

	/**
	 * is named file a directory?
	 *
	 * @return true if directory
	 */
	public static boolean isDirectory(String fileName) {
		return fileName != null && ((new File(fileName)).isDirectory());
	}

	/**
	 * copy a file
	 */
	public static void copyFile(File source, File target) throws IOException {
		copyFile(source.getPath(), target.getPath());
	}

	public static void copyFile(String source, String target) throws IOException {
		try (InputStream ins = getInputStreamPossiblyZIPorGZIP(source); OutputStream outs = getOutputStreamPossiblyZIPorGZIP(target)) {
			ins.transferTo(outs);
		}
	}

	/**
	 * write a stream to a file
	 */
	public static void writeStreamToFile(InputStream ins, File target) throws IOException {
		try (OutputStream outs = getOutputStreamPossiblyZIPorGZIP(target.getPath())) {
			ins.transferTo(outs);
		}
	}

	/**
	 * append a file
	 */
	public static void appendFile(File source, File dest) throws IOException {
		try (FileChannel sourceChannel = new FileInputStream(source).getChannel(); RandomAccessFile raf = new RandomAccessFile(dest, "rw"); FileChannel destChannel = raf.getChannel()) {
			destChannel.transferFrom(sourceChannel, raf.length(), sourceChannel.size());
		}
	}

	/**
	 * open reader
	 */
	public static Reader getReaderPossiblyZIPorGZIP(String fileName) throws IOException {
		return new InputStreamReader(getInputStreamPossiblyZIPorGZIP(fileName));
	}

	/**
	 * gets an input stream. If file ends on gz or zip opens appropriate unzipping stream. Can also be stdin or stdin.gz.
	 * Can also be an URL or an URL ending on .gz
	 */
	public static InputStream getInputStreamPossiblyZIPorGZIP(String fileName) throws IOException {
		if (fileName.endsWith("stdin"))
			return System.in;
		else if (fileName.endsWith("stdin-gz"))
			return new GZIPInputStream(System.in);
		if (isHTTPorFileURL(fileName))
			return getInputStreamPossiblyGZIP(null, fileName);
		final var file = new File(fileName);
		if (file.isDirectory())
			throw new IOException("Directory, not a file: " + file);
		if (!file.exists())
			throw new IOException("No such file: " + file);
		final InputStream ins;
		if (fileName.toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(new FileInputStream(file));
		} else if (fileName.toLowerCase().endsWith(".zip")) {
			var zf = new ZipFile(file);
			var e = zf.entries();
			var entry = (ZipEntry) e.nextElement(); // your only file
			return zf.getInputStream(entry);
		} else
			return new FileInputStream(file);
	}

	/**
	 * opens a file or gzipped file for reading. Can also be stdin or an URL
	 */
	public static InputStream getInputStreamPossiblyGZIP(InputStream ins, String fileName) throws IOException {
		if (fileName.endsWith("stdin"))
			return System.in;
		else if (fileName.endsWith("stdin-gz"))
			return new GZIPInputStream(System.in);
		if (isHTTPorFileURL(fileName)) {
			final URL url = new URL(fileName);
			if (fileName.toLowerCase().endsWith(".gz")) {
				return new GZIPInputStream(url.openStream());
			} else return url.openStream();
		} else if (fileName.toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(ins);
		} else return ins;
	}

	public static Writer getOutputWriterPossiblyZIPorGZIP(String fileName) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(getOutputStreamPossiblyZIPorGZIP(fileName)));
	}

	/**
	 * gets an output stream. If file ends on gz or zip opens appropriate zipping stream. If file equals stdout or stderr, writes to standard out or err
	 */
	public static OutputStream getOutputStreamPossiblyZIPorGZIP(String fileName) throws IOException {
		final String fileNameLowerCase = fileName.toLowerCase();
		switch (fileNameLowerCase) {
			case "stdout":
				return new PrintStreamNoClose(System.out);
			case "stdout-gz":
				return new GZIPOutputStream(new PrintStreamNoClose(System.out));
			case "stderr":
				return new PrintStreamNoClose(System.err);
			case "stderr-gz":
				return new GZIPOutputStream(new PrintStreamNoClose(System.err));
			default:
				OutputStream outs = new FileOutputStream(fileName);
				if (fileNameLowerCase.endsWith(".gz")) {
					outs = new GZIPOutputStream(outs);
				} else if (fileNameLowerCase.endsWith(".zip")) {
					final ZipOutputStream out = new ZipOutputStream(outs);
					ZipEntry e = new ZipEntry(replaceFileSuffix(fileName, ""));
					out.putNextEntry(e);
				}
				return outs;
		}
	}

	/**
	 * is this a gz or zip file?
	 *
	 * @return true, if gz or zip file
	 */
	public static boolean isZIPorGZIPFile(String fileName) {
		return fileName.endsWith(".gz") || fileName.endsWith(".zip");
	}

	public static boolean isHTTPorFileURL(String fileName) {
		return fileName.matches("^(https|http|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	}

	/**
	 * get approximate uncompressed size of file (for use with ProgressListener)
	 *
	 * @return approximate umcompressed size of file
	 */
	public static long guessUncompressedSizeOfFile(String fileName) {
		return (isZIPorGZIPFile(fileName) ? 10 : 1) * (new File(fileName)).length();
	}

	/**
	 * returns a file that has the same given path and one of the given file extensions
	 *
	 * @return file name or null
	 */
	public static String getAnExistingFileWithGivenExtension(String path, final List<String> fileExtensions) {
		if (isZIPorGZIPFile(path))
			path = replaceFileSuffix(path, "");
		String prev;
		do {
			prev = path;
			for (String ext : fileExtensions) {
				final File file = new File(replaceFileSuffix(path, ext));
				if (file.exists())
					return file.getPath();
			}
			path = getFileBaseName(path); // removes last suffix
		}
		while (path.length() < prev.length()); // while a suffix was actually removed
		return null;
	}

	/**
	 * gets a temporary file name modelled on the given name
	 *
	 * @return temporary file name
	 */
	public static String getTemporaryFileName(String name) {
		String zipSuffix = null;
		if (isZIPorGZIPFile(name)) {
			zipSuffix = getFileSuffix(name);
			name = getFileNameWithoutZipOrGZipSuffix(name);
		}
		final var suffix = getFileSuffix(name);
		name = getFileBaseName(name);
		final var number = (int) (System.currentTimeMillis() & ((1 << 20) - 1));
		return String.format("%s-tmp%d.%s%s", name, number, suffix, zipSuffix != null ? "." + zipSuffix : "");
	}

	/**
	 * Returns the path of one File relative to another.
	 *
	 * @param target the target directory
	 * @param base   the base directory
	 * @return target's path relative to the base directory
	 */
	public static File getRelativeFile(File target, File base) {
		if (target.equals(base))
			return new File(".");

		var baseComponents = base.getPath().split(Pattern.quote(File.separator));
		var targetComponents = target.getPath().split(Pattern.quote(File.separator));

		// skip common components
		var index = 0;
		for (; index < targetComponents.length && index < baseComponents.length; ++index) {
			if (!targetComponents[index].equals(baseComponents[index]))
				break;
		}

		var result = new StringBuilder();
		if (index != baseComponents.length) {
			// backtrack to base directory
			for (int i = index; i < baseComponents.length; ++i)
				result.append("..").append(File.separator);
		}
		for (; index < targetComponents.length; ++index)
			result.append(targetComponents[index]).append(File.separator);
		if (!target.getPath().endsWith("/") && !target.getPath().endsWith("\\")) {
			// remove final path separator
			result.delete(result.length() - File.separator.length(), result.length());
		}
		return new File(result.toString());
	}

	/**
	 * gets the file path to the named file using the directory of the referenceFile
	 */
	public static String getFilePath(String referenceFile, String fileName) {
		if (referenceFile == null || referenceFile.length() == 0)
			return fileName;
		else {
			return new File(((new File(referenceFile).getParent())), getFileNameWithoutPath(fileName)).getPath();
		}
	}

	/**
	 * gets the first line in a file. File may be zgipped or zipped
	 *
	 * @return first line or null
	 */
	public static String getFirstLineFromFile(File file) {
		try (BufferedReader ins = new BufferedReader(new InputStreamReader(getInputStreamPossiblyZIPorGZIP(file.getPath())))) {
			return ins.readLine();
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * gets the first line in a file. File may be gzipped or zipped
	 */
	public static String getFirstLineFromFile(File file, String ignoreLinesThatStartWithThis, int maxLines) {
		int count = 0;
		try (BufferedReader ins = new BufferedReader(new InputStreamReader(getInputStreamPossiblyZIPorGZIP(file.getPath())))) {
			String aLine;
			while ((aLine = ins.readLine()) != null) {
				if (!aLine.startsWith(ignoreLinesThatStartWithThis))
					return aLine;
				if (++count == maxLines)
					break;
			}
		} catch (IOException ignored) {
		}
		return null;
	}

	/**
	 * gets the first line in a file. File may be zgipped or zipped
	 */
	public static String[] getFirstLinesFromFile(File file, int count) {
		try (FileLineIterator it = new FileLineIterator(file)) {
			ArrayList<String> lines = new ArrayList<>(count);

			while (it.hasNext()) {
				if (lines.size() < count)
					lines.add(it.next());
				else
					break;
			}
			return lines.toArray(new String[0]);
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * gets the first bytes from a file. File may be zgipped or zipped
	 */
	public static byte[] getFirstBytesFromFile(File file, int maxCount) {
		try (InputStream ins = getInputStreamPossiblyZIPorGZIP(file.getPath())) {
			byte[] buffer = new byte[maxCount];
			final int count = ins.read(buffer, 0, maxCount);

			if (count < maxCount) {
				final byte[] bytes = new byte[count];
				System.arraycopy(buffer, 0, bytes, 0, count);
				return bytes;
			} else
				return buffer;
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * get the number of bytes used to terminate a line
	 *
	 * @return 1 or 2
	 */
	public static int determineEndOfLinesBytes(File file) {
		return determineEndOfLinesBytes(file.getPath());
	}

	/**
	 * get the number of bytes used to terminate a line
	 *
	 * @return 1 or 2
	 */
	public static int determineEndOfLinesBytes(String fileName) {
		var previous = 0;
		try (var r = getReaderPossiblyZIPorGZIP(fileName)) {
			while (r.ready()) {
				var ch = r.read();
				if (ch == '\n') {
					if (previous == '\r')
						return 2;
					else
						return 1;
				}
				previous = ch;
			}
		} catch (IOException ignored) {
		}
		return 1;
	}

	/**
	 * checks that no two of the given files are equal
	 *
	 * @param fileNames (can be null or "")
	 * @return true, if no two files are equal (using File.equals())
	 */
	public static boolean checkAllFilesDifferent(String... fileNames) {
		final File[] files = new File[fileNames.length];
		for (int i = 0; i < fileNames.length; i++) {
			if (fileNames[i] != null && fileNames[i].length() > 0) {
				files[i] = new File(fileNames[i]);
				for (int j = 0; j < i; j++) {
					if (files[i] != null && files[i].equals(files[j]))
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * gets a unique file name for a file in the given directory and with given prefix and suffix
	 *
	 * @return file name
	 */
	public synchronized static File getUniqueFileName(String directory, String prefix, String suffix) {
		File file = new File(directory + File.separatorChar + prefix + (suffix.startsWith(".") ? suffix : "." + suffix));

		int i = 1;
		while (file.exists()) {
			file = new File(directory + File.separatorChar + prefix + "-" + (++i) + (suffix.startsWith(".") ? suffix : "." + suffix));
			if (i == 10000) {
				try {
					return Files.createTempFile(prefix, suffix).toFile(); // too many temporary files in home directory, use tmp dir
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	/**
	 * gets the first line in a file. File may be zgipped or zipped
	 *
	 * @return first line or null
	 */
	public static String getFirstLineFromFileIgnoreEmptyLines(File file, String ignoreLinesThatStartWithThis, int maxLines) {
		try {
			int count = 0;
			try (BufferedReader ins = new BufferedReader(new InputStreamReader(getInputStreamPossiblyZIPorGZIP(file.getPath())))) {
				String aLine;
				while ((aLine = ins.readLine()) != null) {
					if (!aLine.startsWith(ignoreLinesThatStartWithThis) && !aLine.isEmpty())
						return aLine;
					if (++count == maxLines)
						break;
				}
			}
		} catch (IOException ignored) {
		}
		return null;
	}

	/**
	 * get all files in  any of the given directories
	 */
	public static Set<File> getAllFilesInDirectories(Collection<File> rootDirectories, boolean recursively, String... fileExtensions) {
		final Set<File> result = new TreeSet<>();
		for (File rootDirectory : rootDirectories) {
			result.addAll(getAllFilesInDirectory(rootDirectory, recursively, fileExtensions));
		}
		return result;
	}

	/**
	 * get all files listed below the given root directory
	 */
	public static ArrayList<File> getAllFilesInDirectory(File rootDirectory, boolean recursively, String... fileExtensions) {
		final var result = new ArrayList<File>();

		var array = rootDirectory.listFiles();
		if (array != null) {
			Arrays.sort(array);
			final var list = new ArrayList<File>();
			Collections.addAll(list, array);
			while (list.size() > 0) {
				final File file = list.remove(0);
				if (file.isDirectory()) {
					if (recursively) {
						final var below = file.listFiles();
						if (below != null) {
							Arrays.sort(below);
							Collections.addAll(list, below);
						}
					}
				} else if (fileExtensions.length == 0)
					result.add(file);
				else {
					for (var extension : fileExtensions) {
						if (file.getName().endsWith(extension)) {
							result.add(file);
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * get all files listed below the given root directory
	 *
	 * @return list of files
	 */
	public static List<String> getAllFilesInDirectory(String rootDirectoryName, boolean recursively, String... fileExtensions) {
		return getAllFilesInDirectory(new File(rootDirectoryName), recursively, fileExtensions).stream().map(File::getPath).collect(Collectors.toList());
	}

	public static void deleteFileIfExists(String fileName) {
		final File file = new File(fileName);
		if (file.exists())
			file.delete();
	}

	public static boolean clearDirectory(String directory, boolean removeDirectory) {
		return clearDirectory(new File(directory), removeDirectory);
	}

	public static boolean clearDirectory(File directory, boolean removeDirectory) {
		File[] allContents = directory.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				clearDirectory(file, true);
			}
		}
		if (removeDirectory)
			return directory.delete();
		else
			return true;
	}

	public static long getNumberOfLinesInFile(String inputFile) {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStreamPossiblyZIPorGZIP(inputFile)))) {
			return r.lines().count();
		} catch (IOException ex) {
			Basic.caught(ex);
			return 0;
		}
	}

	public static String getFileName(String directory, String name) {
		return (new File(directory, name)).getPath();
	}
}
