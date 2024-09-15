package large.file.distributor;

public class LargeFileDistributorException extends RuntimeException {
    public LargeFileDistributorException(Throwable cause) {
        super("This is my rifle, this is my gun \nFile not found \n" + cause.getMessage(), cause);
    }
}
