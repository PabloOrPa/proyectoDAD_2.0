package sensores_actuadores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SensorTemperaturaListWrapper {
	
	private List<SensorTemperatura> sensoresList;
	
	public SensorTemperaturaListWrapper() {
		super();
	}
	public SensorTemperaturaListWrapper(Collection<SensorTemperatura> sList) {
		super();
		this.sensoresList = new ArrayList<SensorTemperatura>(sList);
	}
	public SensorTemperaturaListWrapper(List<SensorTemperatura> sList) {
		super();
		this.sensoresList = new ArrayList<SensorTemperatura>(sList);
	}
	public List<SensorTemperatura> getSensoresList() {
		return sensoresList;
	}
	public void setSensoresList(List<SensorTemperatura> sensoresList) {
		this.sensoresList = sensoresList;
	}
	@Override
	public int hashCode() {
		return Objects.hash(sensoresList);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorTemperaturaListWrapper other = (SensorTemperaturaListWrapper) obj;
		return Objects.equals(sensoresList, other.sensoresList);
	}
	@Override
	public String toString() {
		return "SensorTemperaturaListWrapper [sensoresList=" + sensoresList + "]";
	}
	
	
}
