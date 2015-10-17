package com.dm.lib;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class DJob extends DJobBase {

    private final HttpURLConnection con;
    private final Path out;

    // TODO cpnnection timeout
    DJob(HttpURLConnection con, Path out, ExecutorService executor) throws IOException {
        super(con.getInputStream(), FileChannel.open(out, CREATE, WRITE),
              con.getContentLengthLong(), executor);
        this.con = con;
        this.out = out;
    }

    public URL getSource() {
        return con.getURL();
    }

    public Path getDestination() {
        return out;
    }

    @Override
    protected void finish(State state, Exception error) {
        super.finish(state, error);
        if (getStatus().getError() != null) {
            try {
                Files.delete(out);
            } catch (IOException ignore){}
        }
    }
}
