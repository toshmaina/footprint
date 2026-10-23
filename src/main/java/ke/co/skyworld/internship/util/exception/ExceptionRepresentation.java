
package ke.co.skyworld.internship.util.exception;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import ke.co.skyworld.internship.util.formatting.DateTime;
import ke.co.skyworld.internship.util.security.UID;

import java.util.Date;
import java.util.List;

@JacksonXmlRootElement(localName = "Exception")
public final class ExceptionRepresentation {

    private final String id;
    private final Date timestamp;
    private final Integer status;
    private final String error;
    private final List<String> errors;
    private final String message;
    private final String path;
    private final String method;

    private ExceptionRepresentation(Builder builder) {
        this.id = new UID().getSSAUUID();
        this.timestamp = DateTime.getCurrentJavaUtilDateTime();
        this.status = builder.status;
        this.error = builder.error;
        this.errors = builder.errors == null
                ? null
                : List.copyOf(builder.errors);
        this.message = builder.message;
        this.path = builder.path;
        this.method = builder.method;
    }

    public String getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer status;
        private String error;
        private List<String> errors;
        private String message;
        private String path;
        private String method;

        private Builder() {
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors == null
                    ? null
                    : List.copyOf(errors);
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public ExceptionRepresentation build() {
            if (status == null) {
                throw new IllegalStateException(
                        "Exception status is required"
                );
            }

            if (status < 100 || status > 599) {
                throw new IllegalStateException(
                        "Invalid HTTP status: " + status
                );
            }

            return new ExceptionRepresentation(this);
        }
    }
}
