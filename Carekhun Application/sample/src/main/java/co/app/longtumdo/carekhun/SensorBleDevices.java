package co.app.longtumdo.carekhun;

public class SensorBleDevices {
	private String name;
	private String address;
	private int rssi;

	public SensorBleDevices() {
	}

	public SensorBleDevices(String name, String address, int rssi) {
		setName(name);
		setAddress(address);
		setRssi(rssi);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
}
