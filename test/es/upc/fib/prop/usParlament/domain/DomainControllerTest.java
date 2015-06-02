package es.upc.fib.prop.usParlament.domain;

import es.upc.fib.prop.usParlament.data.DataControllerImpl;
import es.upc.fib.prop.usParlament.misc.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by ondrej on 1.6.15.
 */
public class DomainControllerTest {

	private final String PATH = "domainControllerTest";
	private final String[] CONGRESS_NAMES = {"congress0", "congress1", "congress2"};
	DomainControllerImpl controller;

	@Before
	public void setUp() throws Exception {
		controller = new DomainControllerImpl();
		controller.setDataController(new DataControllerImpl(PATH));
	}

	@After
	public void tearDown() throws Exception {
		delete(new File(PATH));
	}
	private void delete(File f) throws IOException {
		if (!f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete()) {
			throw new IOException("Failed to delete file: " + f);
		}

	}



	@Test
	public void testCleanCommunityManager() throws Exception {
		Congress congress = prepareCurrentCongress();
		List<Set<MP>> mainPartition = prepareMainPartition();
		List<Set<MP>> partition1 = preparePartition1();
		List<Set<MP>> partition2 = preparePartition2();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		String congressName = CONGRESS_NAMES[0];

		controller.cleanCommunityManager();

		assertEquals(congress, controller.getCurrentCongress());
		assertEquals(new ArrayList<>(), controller.getMainPartition());
		assertEquals(partition1, controller.getPartition1());
		assertEquals(partition2, controller.getPartition2());
		assertEquals(CONGRESS_NAMES[0], controller.getCurrentCongressName());
	}

	@Test
	public void testCleanCompareManager() throws Exception {
		Congress congress = prepareCurrentCongress();
		List<Set<MP>> mainPartition = prepareMainPartition();
		List<Set<MP>> partition1 = preparePartition1();
		List<Set<MP>> partition2 = preparePartition2();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		String congressName = CONGRESS_NAMES[0];

		controller.cleanCompareManager();

		assertEquals(congress, controller.getCurrentCongress());
		assertEquals(mainPartition, controller.getMainPartition());
		assertEquals(new ArrayList<>(), controller.getPartition1());
		assertEquals(new ArrayList<>(), controller.getPartition2());
		assertEquals(CONGRESS_NAMES[0], controller.getCurrentCongressName());
	}

	@Test
	public void testNewCongress() throws Exception {
		Congress congress = prepareCurrentCongress();
		List<Set<MP>> mainPartition = prepareMainPartition();
		List<Set<MP>> partition1 = preparePartition1();
		List<Set<MP>> partition2 = preparePartition2();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		String congressName = CONGRESS_NAMES[0];

		controller.newCongress();

		assertEquals(new Congress(), controller.getCurrentCongress());
		assertEquals(new ArrayList<>(), controller.getMainPartition());
		assertEquals(new ArrayList<>(), controller.getPartition1());
		assertEquals(new ArrayList<>(), controller.getPartition2());
		assertEquals("", controller.getCurrentCongressName());
	}

	@Test
	public void testSaveCurrentCongress() throws Exception {
		Congress congress = prepareCurrentCongress();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		controller.loadCongressAsCurrent(CONGRESS_NAMES[0]);
		Congress current = controller.getCurrentCongress();
		assertEquals(congress, current);
	}
	@Test
	public void testSaveCurrentCongressWithoutName() throws Exception {
		Congress congress = prepareCurrentCongress();
		String exception = controller.saveCurrentCongress("");
		expectedException(IllegalArgumentException.class, exception);
	}
	@Test
	public void testSaveCurrentCongressWithNullName() throws Exception {
		Congress congress = prepareCurrentCongress();
		String exception = controller.saveCurrentCongress(null);
		expectedException(IllegalArgumentException.class, exception);
	}

	@Test
	public void testLoadCongressAsCurrent() throws Exception {
		Congress congress = prepareCurrentCongress();
		controller.saveCurrentCongress(CONGRESS_NAMES[1]);
		controller.loadCongressAsCurrent(CONGRESS_NAMES[1]);
		Congress current = controller.getCurrentCongress();
		assertEquals(congress, current);
	}
	@Test
	public void testLoadCongressAsCurrentNotExist() throws Exception {
		Congress congress = prepareCurrentCongress();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		String exception = controller.loadCongressAsCurrent(CONGRESS_NAMES[1]);
		expectedException(FileNotFoundException.class, exception);
	}
	@Test
	public void testLoadCongressAsCurrentWithNullName() throws Exception {
		Congress congress = prepareCurrentCongress();
		String exception = controller.loadCongressAsCurrent(null);
		expectedException(IllegalArgumentException.class, exception);
	}

	@Test
	public void testLoadAllCongressesNames() throws Exception {
		Congress congress = prepareCurrentCongress();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		controller.saveCurrentCongress(CONGRESS_NAMES[1]);
		controller.saveCurrentCongress(CONGRESS_NAMES[2]);
		String jsonStringNames = controller.loadAllCongressesNames();

		JSONizer json = new JSONizer();
		JSONArray jsonNames = (JSONArray)json.StringToJSON(jsonStringNames).getJSONByKey("congressesNames");
		List<JSON> names = new ArrayList<>(jsonNames.getArray());

		List<String> sringNames = new ArrayList<>();
		for (JSON name : names) {
			sringNames.add(((JSONString)name).getValue());
		}

		List<String> expected = new ArrayList<>();
		for (String name : CONGRESS_NAMES) {
			expected.add(name);
		}
		Collections.sort(expected);
		Collections.sort(sringNames);
		assertEquals(expected, sringNames);
	}

	@Test
	public void testGetCurrentCongressName() throws Exception {
		Congress congress = prepareCurrentCongress();
		controller.saveCurrentCongress(CONGRESS_NAMES[0]);
		String congressName = controller.getCurrentCongressName();
		assertEquals(CONGRESS_NAMES[0], congressName);
	}
	@Test
	public void testGetCurrentCongressNameUnsaved() throws Exception {
		Congress congress = prepareCurrentCongress();
		String congressName = controller.getCurrentCongressName();
		assertEquals("", congressName);
	}

	@Test
	public void testComputeRelationships() throws Exception {
		Congress congress = prepareCurrentCongress();

		controller.computeRelationships();

		Congress expected = prepareCongressWithWeights();

		assertEquals(expected, controller.getCurrentCongress());
	}

	@Test
	public void testGetMPsShort() throws Exception {
		Congress congress = prepareCurrentCongress();
		String current = controller.getMPsShort();

		String expected = "{\"MPList\":[{\"State\":\"CO\",\"District\":\"2\"}," +
				"{\"State\":\"NY\",\"District\":\"1\"},{\"State\":\"CO\",\"District\":\"1\"}," +
				"{\"State\":\"OH\",\"District\":\"2\"},{\"State\":\"CA\",\"District\":\"1\"}," +
				"{\"State\":\"WA\",\"District\":\"1\"}]}";

		JSONizer json = new JSONizer();

		assertEquals(json.StringToJSON(expected), json.StringToJSON(current));
	}

	@Test
	public void testGetMPs() throws Exception {
		Congress congress = prepareCurrentCongress();
		String current = controller.getMPs();

		String expected = "{\"MPList\":[{\"Name\":\"Aleix\",\"religion\":\"islamism\",\"sex\":\"male\"," +
				"\"State\":\"WA\",\"District\":\"1\",\"sport\":\"football\",\"party\":\"democrat\"}," +
				"{\"Name\":\"Alex\",\"religion\":\"catholicism\",\"sex\":\"male\"," +
				"\"State\":\"NY\",\"District\":\"1\",\"sport\":\"football\",\"party\":\"republican\"}," +
				"{\"Name\":\"Homer\",\"religion\":\"islamism\"," +
				"\"State\":\"CO\",\"District\":\"2\",\"sport\":\"basketball\"}," +
				"{\"Name\":\"Kate\",\"religion\":\"judaism\",\"sex\":\"female\"," +
				"\"State\":\"OH\",\"District\":\"2\",\"party\":\"republican\"}," +
				"{\"Name\":\"Miquel\",\"religion\":\"catholicism\",\"sex\":\"male\"," +
				"\"State\":\"CO\",\"District\":\"1\",\"sport\":\"football\",\"party\":\"democrat\"}," +
				"{\"Name\":\"Ondrej\",\"sex\":\"male\"," +
				"\"State\":\"CA\",\"District\":\"1\",\"sport\":\"hockey\",\"party\":\"republican\"}]}";

		JSONizer json = new JSONizer();

		assertEquals(json.StringToJSON(expected), json.StringToJSON(current));
	}

	@Test
	public void testGetMP() throws Exception {
		prepareCurrentCongress();

		JSONObject jo = new JSONObject();
		jo.addPair(new JSONString("State"),new JSONString("NY"));
		jo.addPair(new JSONString("District"),new JSONString("1"));
		jo.addPair(new JSONString("Name"),new JSONString("Alex"));
		JSONArray ja = new JSONArray();

		JSONObject j = new JSONObject();
		j.addPair(new JSONString("AttrDefName"), new JSONString("religion"));
		j.addPair(new JSONString("AttrValue"),new JSONString("catholicism"));
		ja.addElement(j);

		j = new JSONObject();
		j.addPair(new JSONString("AttrDefName"), new JSONString("sex"));
		j.addPair(new JSONString("AttrValue"),new JSONString("male"));
		ja.addElement(j);

		j = new JSONObject();
		j.addPair(new JSONString("AttrDefName"), new JSONString("sport"));
		j.addPair(new JSONString("AttrValue"),new JSONString("football"));
		ja.addElement(j);

		j = new JSONObject();
		j.addPair(new JSONString("AttrDefName"), new JSONString("party"));
		j.addPair(new JSONString("AttrValue"),new JSONString("republican"));
		ja.addElement(j);

		jo.addPair(new JSONString("Attributes"),ja);
		String alex = jo.stringify();

        /*String alex = new String("{\"State\":\"CA\",\"District\":\"2\",\"Name\":\"Alex\",\"Attributes\":[" +
                "{\"AttrDefName\":\"sex\",\"AttrValue\":\"male\"},{\"AttrDefName\":\"party\",\"AttrValue\":\"democrat\"}," +
                "{\"AttrDefName\":\"religion\",\"AttrValue\":\"catholicism\"}]}");//*/
		String current = controller.getMP(State.NY, 1);
		assertEquals(alex, current);
	}

	@Test
	public void testAddMP() throws Exception {
		Congress congress = prepareCurrentCongress();
		controller.addMP("{\"Name\":\"Aleix\",\"State\":\"WA\",\"District\":\"2\"}");
		assertNotNull(congress.getMP(State.WA, 2));
	}
	@Test
	public void testAddMPWithoutName() throws Exception {
		Congress congress = prepareCurrentCongress();
		String exception = controller.addMP("{\"State\":\"WA\",\"District\":\"2\"}");
		expectedException(IllegalArgumentException.class, exception);
	}
	@Test
	public void testAddMPWithoutState() throws Exception {
		Congress congress = prepareCurrentCongress();
		String exception = controller.addMP("{\"Name\":\"Aleix\",\"District\":\"2\"}");
		expectedException(IllegalArgumentException.class, exception);
	}
	@Test
	public void testAddMPWithoutDistrict() throws Exception {
		Congress congress = prepareCurrentCongress();
		String exception = controller.addMP("{\"Name\":\"Aleix\",\"State\":\"WA\"}");
		expectedException(IllegalArgumentException.class, exception);
	}

	@Test
	public void testRemoveMP() throws Exception {

	}

	@Test
	public void testRemoveMP1() throws Exception {

	}

	@Test
	public void testGetAttrDefs() throws Exception {

	}

	@Test
	public void testAddOrModifyAttrDef() throws Exception {

	}

	@Test
	public void testDeleteAttrDef() throws Exception {

	}

	@Test
	public void testAddOrModifyAttribute() throws Exception {

	}

	@Test
	public void testAddOrModifyAttributes() throws Exception {

	}

	@Test
	public void testRemoveAttribute() throws Exception {

	}

	@Test
	public void testExistsAttrDef() throws Exception {

	}

	@Test
	public void testSaveMainPartition() throws Exception {

	}

	@Test
	public void testLoadPartitionInto() throws Exception {

	}

	@Test
	public void testLoadAllPartitionsInCurrentCongress() throws Exception {

	}

	@Test
	public void testGetCommunityIDs() throws Exception {

	}

	@Test
	public void testGetMainPartitionSize() throws Exception {

	}

	@Test
	public void testGetMPsInMainPartition() throws Exception {

	}

	@Test
	public void testGetMPsInPartition1() throws Exception {

	}

	@Test
	public void testGetMPsInPartition2() throws Exception {

	}

	@Test
	public void testSetMainToPartition1() throws Exception {

	}

	@Test
	public void testSetMainToPartition2() throws Exception {

	}

	@Test
	public void testCompare2partitions() throws Exception {

	}

	@Test
	public void testLoadAllPartitionNamesInCurrentCongress() throws Exception {

	}

	@Test
	public void testComputePartition() throws Exception {

	}

	@Test
	public void testAddNewCommunity() throws Exception {

	}

	@Test
	public void testRemoveCommunity() throws Exception {

	}

	@Test
	public void testAddMPToCommunity() throws Exception {

	}

	@Test
	public void testRemoveMPFromCommunity() throws Exception {

	}

	private Congress prepareCurrentCongress() {
		Congress congress = prepareCongress();
		controller.setCurrentCongress(congress);
		return congress;
	}
	private Congress prepareCongress() {
		Congress congress = new Congress();

		AttrDefinition sex = new AttrDefinition("sex", 1);
		AttrDefinition sport = new AttrDefinition("sport", 1);
		AttrDefinition religion = new AttrDefinition("religion", 4);
		AttrDefinition party = new AttrDefinition("party", 16);
		congress.addAttrDef(sex);
		congress.addAttrDef(sport);
		congress.addAttrDef(religion);
		congress.addAttrDef(party);

		MP ondrej = new MP("Ondrej", State.CA, 1);
		MP alex = new MP("Alex", State.NY, 1);
		MP aleix = new MP("Aleix", State.WA, 1);
		MP miquel = new MP("Miquel", State.CO, 1);
		MP homer = new MP("Homer", State.CO, 2);
		MP kate = new MP("Kate", State.OH, 2);

		Attribute male = new Attribute(congress.getAttrDef("sex"), "male");
		Attribute female = new Attribute(congress.getAttrDef("sex"), "female");

		Attribute football = new Attribute(congress.getAttrDef("sport"), "football");
		Attribute hockey = new Attribute(congress.getAttrDef("sport"), "hockey");
		Attribute basketball = new Attribute(congress.getAttrDef("sport"), "basketball");

		Attribute catholicism = new Attribute(congress.getAttrDef("religion"), "catholicism");
		Attribute islamism = new Attribute(congress.getAttrDef("religion"), "islamism");
		Attribute judaism = new Attribute(congress.getAttrDef("religion"), "judaism");

		Attribute democrat = new Attribute(congress.getAttrDef("party"), "democrat");
		Attribute republican = new Attribute(congress.getAttrDef("party"), "republican");

		ondrej.addAttribute(male);
		ondrej.addAttribute(hockey);
		ondrej.addAttribute(republican);

		miquel.addAttribute(male);
		miquel.addAttribute(football);
		miquel.addAttribute(catholicism);
		miquel.addAttribute(democrat);

		alex.addAttribute(male);
		alex.addAttribute(football);
		alex.addAttribute(catholicism);
		alex.addAttribute(republican);

		aleix.addAttribute(male);
		aleix.addAttribute(football);
		aleix.addAttribute(islamism);
		aleix.addAttribute(democrat);

		homer.addAttribute(basketball);
		homer.addAttribute(islamism);

		kate.addAttribute(female);
		kate.addAttribute(judaism);
		kate.addAttribute(republican);

		congress.addNode(ondrej);
		congress.addNode(miquel);
		congress.addNode(alex);
		congress.addNode(aleix);
		congress.addNode(kate);
		congress.addNode(homer);

		return congress;
	}
	private List<Set<MP>>  prepareMainPartition() {
		List<Set<MP>> partition = controller.getMainPartition();
		Congress congress = controller.getCurrentCongress();

		Set<MP> comm1 = new HashSet<>();
		Set<MP> comm2 = new HashSet<>();
		Set<MP> comm3 = new HashSet<>();

		List<MP> mps = new ArrayList<MP>(congress.getMPs());
		comm1.add(mps.get(0));
		comm1.add(mps.get(1));
		comm1.add(mps.get(2));
		comm2.add(mps.get(2));
		comm2.add(mps.get(3));
		comm3.add(mps.get(4));
		comm3.add(mps.get(5));

		partition.add(comm1);
		partition.add(comm2);
		partition.add(comm3);

		return partition;
	}
	private List<Set<MP>>  preparePartition1() {
		List<Set<MP>> partition = controller.getPartition1();
		Congress congress = controller.getCurrentCongress();

		Set<MP> comm1 = new HashSet<>();
		Set<MP> comm2 = new HashSet<>();
		Set<MP> comm3 = new HashSet<>();

		List<MP> mps = new ArrayList<MP>(congress.getMPs());
		comm1.add(mps.get(5));
		comm1.add(mps.get(4));
		comm1.add(mps.get(3));
		comm2.add(mps.get(2));
		comm2.add(mps.get(1));
		comm3.add(mps.get(1));
		comm3.add(mps.get(0));

		partition.add(comm1);
		partition.add(comm2);
		partition.add(comm3);

		return partition;
	}
	private List<Set<MP>>  preparePartition2() {
		List<Set<MP>> partition = controller.getPartition2();
		Congress congress = controller.getCurrentCongress();

		Set<MP> comm1 = new HashSet<>();
		Set<MP> comm2 = new HashSet<>();

		List<MP> mps = new ArrayList<MP>(congress.getMPs());
		comm1.add(mps.get(0));
		comm1.add(mps.get(1));
		comm1.add(mps.get(2));
		comm1.add(mps.get(3));
		comm2.add(mps.get(3));
		comm2.add(mps.get(4));

		partition.add(comm1);
		partition.add(comm2);

		return partition;
	}
	private Congress prepareCongressWithWeights() {
		Congress c = prepareCongress();

		c.addEdge(new Relationship(c.getMP(State.WA, 1), c.getMP(State.CO, 2), 4));
		c.addEdge(new Relationship(c.getMP(State.WA, 1), c.getMP(State.CO, 1), 18));
		c.addEdge(new Relationship(c.getMP(State.WA, 1), c.getMP(State.NY, 1), 2));
		c.addEdge(new Relationship(c.getMP(State.CO, 1), c.getMP(State.CA, 1), 1));
		c.addEdge(new Relationship(c.getMP(State.OH, 2), c.getMP(State.CA, 1), 16));
		c.addEdge(new Relationship(c.getMP(State.NY, 1), c.getMP(State.CA, 1), 17));
		c.addEdge(new Relationship(c.getMP(State.NY, 1), c.getMP(State.CO, 1), 6));
		c.addEdge(new Relationship(c.getMP(State.WA, 1), c.getMP(State.CA, 1), 1));
		c.addEdge(new Relationship(c.getMP(State.NY, 1), c.getMP(State.OH, 2), 16));

		controller.setCurrentCongress(c);
		return c;
	}

	private void expectedException(Class exception, String jsonStringException) {
		JSONizer json = new JSONizer();
		JSONObject jsonException = (JSONObject)json.StringToJSON(jsonStringException).getJSONByKey("Exception");
		assertNotNull(jsonException);
		String currentException = ((JSONString)jsonException.getJSONByKey("Name")).getValue();
		assertEquals(exception.getSimpleName(), currentException);
	}
}