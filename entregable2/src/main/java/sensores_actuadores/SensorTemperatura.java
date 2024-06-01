package sensores_actuadores;

import java.util.Objects;

public class SensorTemperatura {

	private Integer idTemp;
	private Double valor;
	private Long timestamp;
	private Integer idPlaca;
	private Integer idGroup;
	private Integer idDB;
	
	public SensorTemperatura(Integer idTemp, Double valor, Integer idPlaca, Integer idGroup, Integer idDB) {
		super();
		this.idTemp = idTemp;
		this.valor = valor;
		this.timestamp = System.currentTimeMillis();
		this.idPlaca = idPlaca;
		this.idGroup = idGroup;
		this.idDB = idDB;
	}

	public Integer getIdTemp() {
		return idTemp;
	}

	public void setIdTemp(Integer idTemp) {
		this.idTemp = idTemp;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}

	public Integer getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idDB, idGroup, idPlaca, idTemp, timestamp, valor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorTemperatura other = (SensorTemperatura) obj;
		return Objects.equals(idDB, other.idDB) && Objects.equals(idGroup, other.idGroup)
				&& Objects.equals(idPlaca, other.idPlaca) && Objects.equals(idTemp, other.idTemp)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(valor, other.valor);
	}

	@Override
	public String toString() {
		return "SensorTemperatura [idTemp=" + idTemp + ", valor=" + valor + ", timestamp=" + timestamp + ", idPlaca="
				+ idPlaca + ", idGroup=" + idGroup + ", idDB=" + idDB + "]";
	}
	
	
	
	
	
	
	
}

