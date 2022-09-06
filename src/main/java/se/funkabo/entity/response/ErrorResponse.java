package se.funkabo.entity.response;



public class ErrorResponse {

	private String timestamp;
	private String status;
	private String message;
	
	public ErrorResponse() {}
	
	public ErrorResponse(String timestamp, 
			String status, 
				         String message) {
		this.timestamp = timestamp;
		this.status = status;
		this.message = message;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
