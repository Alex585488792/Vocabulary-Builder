import java.time.LocalDate;
import java.util.ArrayList;

public class Word {
	private String name;
	private String pos;
	private ArrayList<String> meaning;
	private LocalDate dateAdded, dateRevise, dateComplete;
	private int lv, noOfMeanings, id, nDontRemember;
	public Word(String n, String m) {
		name = n;
		ArrayList<String> mArr = new ArrayList<String>();
		mArr.add(m);
		meaning = mArr;
	}
	public Word(String n, String ps, String m) {
		name = n;
		pos = ps;
		ArrayList<String> mArr = new ArrayList<String>();
		mArr.add(m);
		meaning = mArr;
	}
	public Word(int i, String n, String p, ArrayList<String> m, int lvl,LocalDate da, LocalDate dc, LocalDate dr) {
		id = i;
		name = n;
		pos = p;
		meaning = m;
		noOfMeanings = m.size();
		lv = lvl;
		dateAdded = da;
		dateComplete = dc;
		dateRevise = dr;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return this.id;
	}
	public void setName(String n) {
		this.name = n;
	}
	public String getName() {
		return this.name;
	}
	public void setPOS(String p) {
		this.pos = p;
	}
	public String getPOS() {
		return this.pos;
	}
	public void setMeaning(String m, int i) {
		this.meaning.set(i, m);
	}
	public String getMeaning() {
		String m = "";
		for (int i = 0; i < this.noOfMeanings; i++) {
			m = m.concat(this.meaning.get(i));
			m = m.concat("\n");
		}
		return m;
	}
	public String getMeaning(int i) {
		return this.meaning.get(i);
	}
	public void addMeaning(String [] meaning) {
		for (int i = 0; i < meaning.length; i++) {
			this.meaning.add(meaning[i]);
		}
		this.noOfMeanings = this.meaning.size();
	}
	public void removeMeaning(int i) {
		this.meaning.remove(i);
		this.noOfMeanings--;
	}
	public int getNumMeaning() {
		return this.noOfMeanings;
	}
	public void incLevel() {
		if (nDontRemember < 2) {
			this.lv++;	
		}
		nDontRemember = 0;
		if (lv == 10) {
			this.dateRevise = LocalDate.MIN;
		}
	}
	public int getLevel() {
		return this.lv;
	}
	public void setDateAdded(LocalDate d) {
		this.dateAdded = d;
	}
	public LocalDate getDateAdded() {
		return this.dateAdded;
	}
	public void setDateCompleted(LocalDate d) {
		this.dateComplete = d;
	}
	public LocalDate getDateCompleted() {
		return this.dateComplete;
	}
	public LocalDate getDateRevise() {
		return this.dateRevise;
	}
	public void setNewRevDate() {
		this.dateRevise = LocalDate.now().plusDays(2 * this.lv);
	}
	public void dontRemember() {
		this.nDontRemember++;
	}
	public void delete() {
		name = "";
		meaning.clear();
		pos = "";
	}
}
