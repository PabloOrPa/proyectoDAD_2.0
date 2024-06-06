package sensores_actuadores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SensorLuzListWrapper {
	
	private List<SensorLuz> sensoresList;
	
	public SensorLuzListWrapper() {
		super();
	}
	public SensorLuzListWrapper(Collection<SensorLuz> sList) {
		super();
		this.sensoresList = new ArrayList<SensorLuz> (sList);
	}
	public SensorLuzListWrapper(List<SensorLuz> sList) {
		super();
		this.sensoresList = new ArrayList<SensorLuz> (sList);
	}
	public List<SensorLuz> getSensoresList() {
		return sensoresList;
	}
	public void setSensoresList(List<SensorLuz> sensoresList) {
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
		SensorLuzListWrapper other = (SensorLuzListWrapper) obj;
		return Objects.equals(sensoresList, other.sensoresList);
	}
	@Override
	public String toString() {
		return "SensorLuzListWrapper [sensoresList=" + sensoresList + "]";
	}
	
	
}
