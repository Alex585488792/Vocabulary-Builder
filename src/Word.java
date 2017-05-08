import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Word {
	public DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	String name;
	String phonetic;
	String [] meaning;
	String dateAdded, dateCompleted;
	Boolean rev;
	int lv, noOfMeanings;
	public Word() {
		name = "";
		meaning = new String[5];
		dateAdded = df.format(Calendar.getInstance().getTime());
		dateCompleted = "";
		lv = 0;
		rev = false;
	}
	public Word(String n, String p, String[] m, int lvl,String da, String dc, Boolean revi) {
		name = n;
		p = phonetic;
		meaning = new String[5];
		meaning = m;
		noOfMeanings = m.length;
		dateAdded = da;
		rev = revi;
		dateCompleted = dc;
	}
}
