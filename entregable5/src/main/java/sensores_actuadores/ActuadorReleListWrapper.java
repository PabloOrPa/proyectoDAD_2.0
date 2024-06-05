package sensores_actuadores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ActuadorReleListWrapper {
	private List<ActuadorRele> releList;
	
	public ActuadorReleListWrapper() {
		super();
	}
	
	public ActuadorReleListWrapper(Collection<ActuadorRele> releList) {
		super();
		this.releList = new ArrayList<ActuadorRele>(releList);
	}
	
	public ActuadorReleListWrapper(List<ActuadorRele> releList) {
		super();
		this.releList = new ArrayList<ActuadorRele>(releList);
	}
	
	public List<ActuadorRele> getReleList(){
		return releList;
	}
	
	public void setReleList(List<ActuadorRele> releList) {
		this.releList = releList;
	}

	@Override
	public int hashCode() {
		return Objects.hash(releList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorReleListWrapper other = (ActuadorReleListWrapper) obj;
		return Objects.equals(releList, other.releList);
	}

	@Override
	public String toString() {
		return "ActuadorReleListWrapper [releList=" + releList + "]";
	}
	
	
}
