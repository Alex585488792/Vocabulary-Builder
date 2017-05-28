import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Word {
	public DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private String name;
	private String phonetic;
	private ArrayList<String> meaning;
	private String dateAdded, dateCompleted;
	private Boolean rev;
	private int lv, noOfMeanings;
//	public Word() {
//		name = "";
//		meaning = new String[5];
//		dateAdded = new Date();
//		dateCompleted = null;
//		lv = 0;
//		rev = false;
//	}
	public Word(String n, String p, ArrayList<String> m, int lvl,String da, String dc, Boolean revi) {
		name = n;
		phonetic = p;
		meaning = m;
		noOfMeanings = m.size();
		dateAdded = da;
		rev = revi;
		dateCompleted = dc;
	}
	public void setName(String n) {
		this.name = n;
	}
	public String getName() {
		return this.name;
	}
	public void setPhonetic(String p) {
		this.phonetic = p;
	}
	public String getPhonetic() {
		return this.phonetic;
	}
	public void setMeaning(String m, int i) {
		this.meaning.set(i, m);
	}
	public String getMeaning(int i) {
		return this.meaning.get(i);
	}
	public int getNumMeaning() {
		return this.noOfMeanings;
	}
	public void incLevel() {
		this.lv++;
	}
	public int getLevel() {
		return this.lv;
	}
	public void setDateAdded(String d) {
		this.dateAdded = d;
	}
	public String getDateAdded() {
		return this.dateAdded;
	}
	public void setDateCompleted(String d) {
		this.dateCompleted = d;
	}
	public String getDateCompleted() {
		return this.dateCompleted;
	}
	public void setRev(Boolean b) {
		this.rev = b;
	}
	public Boolean getRev() {
		return this.rev;
	}
}
