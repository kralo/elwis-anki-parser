import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class AnkiSbfGenerator {

	static class Frage {
		String fragentext = "";
		String answer = "";
		String distractor1 = "";
		String distractor2 = "";
		String distractor3 = "";
		Integer fragennr = 0;

		public boolean isVollstaendig() {
			return fragentext.length() > 2 && answer.length() > 2 && distractor1.length() > 2 && distractor2.length() > 2 && distractor3.length() > 3;
		}
	}

	HashMap<String, Integer> media = new HashMap<String, Integer>();
	private String mediafolder = "";
	String output = "";

	public void parse(String URL, String tags) throws IOException {
		Document doc = Jsoup.connect(URL).get();

		Element newsHeadlines = doc.select("#content").first();

		Frage lFrage = null;

		for (Element headline : newsHeadlines.children()) {

			if ((headline.tagName().equals("p") && headline.hasClass("line")) || headline.hasClass("dateOfIssue") && lFrage.isVollstaendig()) {

				// Ende der Frage.
				if (lFrage.isVollstaendig())
					output += String.format("%03d;(%03d) %s;%s;%s;%s;%s;%s\n", lFrage.fragennr, lFrage.fragennr, lFrage.fragentext.trim(), lFrage.answer, lFrage.distractor1,
							lFrage.distractor2, lFrage.distractor3, tags);
				lFrage = null;
			} else if (headline.hasClass("picture")) {
				// Bilder
				try {
					String filename = Paths.get(new URI(headline.select("img").first().absUrl("src")).getPath()).getFileName().toString();
					lFrage.fragentext += "<img src=\"" + filename + "\" />";
					if (!media.containsKey(filename))
						media.put(filename, media.keySet().size() + 1);
				} catch (URISyntaxException e) {
				}
			} else if (headline.tagName().equalsIgnoreCase("p")) {
				if (lFrage == null) {
					lFrage = new Frage();
					try {
						lFrage.fragennr = Integer.valueOf(headline.text().substring(0, headline.text().indexOf(".")));
					} catch (Exception e) {
					}
					lFrage.fragentext = headline.text().substring(headline.text().indexOf(".") + 1).replace(";", ",");
				} else
					lFrage.fragentext += headline.text();
			} else if (headline.tagName().equalsIgnoreCase("ol")) {
				// Antworten
				int i = 0;
				for (Element ele : headline.select("li")) {
					switch (i) {
					case 0:
						lFrage.answer = ele.text().replace(";", ",");
						break;
					case 1:
						lFrage.distractor1 = ele.text().replace(";", ",");
						break;
					case 2:
						lFrage.distractor2 = ele.text().replace(";", ",");
						break;
					default:
						lFrage.distractor3 = ele.text().replace(";", ",");
						break;
					}
					i++;
				}
			}

		}
	}

	public static void main(String[] args) throws IOException {
		{
			AnkiSbfGenerator t = new AnkiSbfGenerator();
			String FOLDER = "/dev/shm/elwis-anki-parser/img-binnen/";
			t.mediafolder(FOLDER);
			t.parse("https://www.elwis.de/DE/Sportschifffahrt/Sportbootfuehrerscheine/Fragenkatalog-Binnen-neu/Basisfragen/Basisfragen-node.html", "SBF-Binnen-Basisfragen2023");
			t.parse("https://www.elwis.de/DE/Sportschifffahrt/Sportbootfuehrerscheine/Fragenkatalog-Binnen-neu/Spezifische-Fragen-Binnen/Spezifische-Fragen-Binnen-node.html",
					"SBF-Binnen-Spezifisch-Binnen2023");
			t.parse("https://www.elwis.de/DE/Sportschifffahrt/Sportbootfuehrerscheine/Fragenkatalog-Binnen-neu/Spezifische-Fragen-Segeln/Spezifische-Fragen-Segeln-node.html",
					"SBF-Binnen-Spezifisch-Segeln2023");

			BufferedWriter writer = new BufferedWriter(new FileWriter(FOLDER + "gen-binnen.csv"));
			writer.write(t.getOutput());
			writer.close();

			BufferedWriter writer2 = new BufferedWriter(new FileWriter(FOLDER + "media"));
			writer2.write(t.renameMediaAndGetMediaFile());
			writer2.close();
		}

		{
			AnkiSbfGenerator t = new AnkiSbfGenerator();
			String FOLDER = "/dev/shm/elwis-anki-parser/img-see/";
			t.mediafolder(FOLDER);
			t.parse("https://www.elwis.de/DE/Sportschifffahrt/Sportbootfuehrerscheine/Fragenkatalog-See-neu/Basisfragen/Basisfragen-node.html", "SBF-See-Basisfragen-2023");
			t.parse("https://www.elwis.de/DE/Sportschifffahrt/Sportbootfuehrerscheine/Fragenkatalog-See-neu/Spezifische-Fragen-See/Spezifische-Fragen-See-node.html",
					"SBF-See-Spezifische-Fragen-2023");
			BufferedWriter writer = new BufferedWriter(new FileWriter(FOLDER + "gen-see.csv"));
			writer.write(t.getOutput());
			writer.close();

			BufferedWriter writer2 = new BufferedWriter(new FileWriter(FOLDER + "media"));
			writer2.write(t.renameMediaAndGetMediaFile());
			writer2.close();

		}
	}

	private String getOutput() {
		return output;
	}

	private void mediafolder(String string) {
		this.mediafolder = string;

	}

	private String renameMediaAndGetMediaFile() {
		ArrayList<String> kvJSON = new ArrayList<String>();
		for (String fn : media.keySet()) {

			Integer id = media.get(fn);
			String newfile = String.format("%03d", id) + "" + Long.toHexString(Double.doubleToLongBits(Math.random()));
			Path copied = Paths.get(mediafolder + newfile);
			Path originalPath = Paths.get(mediafolder + fn);
			try {
				Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			kvJSON.add(String.format("\"%s\":\"%s\"", newfile, fn));
		}

		return "{" + String.join(",", kvJSON) + "}";
	}

}
