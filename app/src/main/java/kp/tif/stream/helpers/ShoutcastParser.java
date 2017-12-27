package kp.tif.stream.helpers;

import java.io.IOException;
import java.net.URISyntaxException;
import net.moraleboost.streamscraper.ScrapeException;


/**
 * Created by Synchziro.
 */
public class ShoutcastParser {
    public String getStreamGenere(String streamURL) throws IOException, URISyntaxException, ScrapeException {
        ShoutCastMetadataRetriever smr = new ShoutCastMetadataRetriever();
        smr.setDataSource(streamURL);
        String genre = smr.extractMetadata(ShoutCastMetadataRetriever.METADATA_KEY_GENRE);
        return genre;
    }
    public String getStreamTitle(String streamURL) throws IOException, URISyntaxException, ScrapeException {
        ShoutCastMetadataRetriever smr = new ShoutCastMetadataRetriever();
        smr.setDataSource(streamURL);
        String title = smr.extractMetadata(ShoutCastMetadataRetriever.METADATA_KEY_TITLE);
        return title;
    }

}
