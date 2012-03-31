package sat.tests.radio;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import sat.radio.RadioID;

public class RadioIDTest {
	@Test
	public void testLegacyEquality() {
		RadioID id1 = new RadioID(new byte[] { 'H', 'E', 'L', 'L', 'O' });
		RadioID id2 = new RadioID(new byte[] { 'H', 'E', 'L', 'L', 'O' });

		assertEquals(id1.hashCode(), id2.hashCode());
		assertTrue(id1.equals(id2));
	}

	@Test
	public void testExtendedSerializedEquality() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		// Serialize the first ID
		RadioID id1 = new RadioID("PLN");
		oos.writeObject(id1);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);

		// Deserialize it to a second ID
		RadioID id2 = (RadioID) ois.readObject();

		assertEquals(id1.hashCode(), id2.hashCode());
		assertTrue(id1.equals(id2));

	}

	@Test
	public void testExtendedInquality() throws Exception {
		RadioID id1 = new RadioID("PLN");
		RadioID id2 = new RadioID("PLN");

		assertFalse(id1.hashCode() == id2.hashCode());
		assertFalse(id1.equals(id2));
	}
}
