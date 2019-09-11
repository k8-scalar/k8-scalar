package be.kuleuven.distrinet.scalar.requests;

import be.kuleuven.distrinet.scalar.cassandra.DatastaxCassandraClient;
import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import java.time.LocalDateTime;

public class CassandraWriteRequest extends Request {
    public CassandraWriteRequest(User usr) {
        super(usr, true);
    }

    public void doRequest() throws RequestException {
        Log log = createLog();

        user().targetUrl();

        DatastaxCassandraClient cassandra = DatastaxCassandraClient.getInstance(user().targetUrl());
        try {
            startTimer();
            cassandra.write(log);
            stopTimer();

            done(RequestResult.SUCCEEDED);
        } catch (Exception e) {
            System.out.println("### Write request failed, reason:");
            e.printStackTrace();
            done(RequestResult.FAILED);
        }
    }

    private Log createLog() {
        LogId logId = LogId.create(user(), user().loopCount());
        LocalDateTime timestamp = LocalDateTime.now();

        return new Log(logId, timestamp, RANDOM_LOG_MESSAGE);
    }

    private static final String RANDOM_LOG_MESSAGE = "SSBoYXZlIG1ldCB0aGVtIGF0IGNsb3NlIG9mIGRheQ=="
            + "Q29taW5nIHdpdGggdml2aWQgZmFjZXM="
            + "RnJvbSBjb3VudGVyIG9yIGRlc2sgYW1vbmcgZ3JleQ=="
            + "RWlnaHRlZW50aC1jZW50dXJ5IGhvdXNlcy4="
            + "SSBoYXZlIHBhc3NlZCB3aXRoIGEgbm9kIG9mIHRoZSBoZWFk"
            + "T3IgcG9saXRlIG1lYW5pbmdsZXNzIHdvcmRzLA=="
            + "T3IgaGF2ZSBsaW5nZXJlZCBhd2hpbGUgYW5kIHNhaWQ="
            + "UG9saXRlIG1lYW5pbmdsZXNzIHdvcmRzLA=="
            + "QW5kIHRob3VnaHQgYmVmb3JlIEkgaGFkIGRvbmU="
            + "T2YgYSBtb2NraW5nIHRhbGUgb3IgYSBnaWJl"
            + "VG8gcGxlYXNlIGEgY29tcGFuaW9u"
            + "QXJvdW5kIHRoZSBmaXJlIGF0IHRoZSBjbHViLA=="
            + "QmVpbmcgY2VydGFpbiB0aGF0IHRoZXkgYW5kIEk="
            + "QnV0IGxpdmVkIHdoZXJlIG1vdGxleSBpcyB3b3JuOg=="
            + "QWxsIGNoYW5nZWQsIGNoYW5nZWQgdXR0ZXJseTo="
            + "QSB0ZXJyaWJsZSBiZWF1dHkgaXMgYm9ybi4="
            + "VGhhdCB3b21hbidzIGRheXMgd2VyZSBzcGVudA=="
            + "SW4gaWdub3JhbnQgZ29vZCB3aWxsLA=="
            + "SGVyIG5pZ2h0cyBpbiBhcmd1bWVudA=="
            + "VW50aWwgaGVyIHZvaWNlIGdyZXcgc2hyaWxsLg=="
            + "V2hhdCB2b2ljZSBtb3JlIHN3ZWV0IHRoYW4gaGVycw=="
            + "V2hlbiB5b3VuZyBhbmQgYmVhdXRpZnVsLA=="
            + "U2hlIHJvZGUgdG8gaGFycmllcnM/"
            + "VGhpcyBtYW4gaGFkIGtlcHQgYSBzY2hvb2w="
            + "QW5kIHJvZGUgb3VyIHdpbmdlZCBob3JzZS4="
            + "VGhpcyBvdGhlciBoaXMgaGVscGVyIGFuZCBmcmllbmQ="
            + "V2FzIGNvbWluZyBpbnRvIGhpcyBmb3JjZTs="
            + "SGUgbWlnaHQgaGF2ZSB3b24gZmFtZSBpbiB0aGUgZW5kLA=="
            + "U28gc2Vuc2l0aXZlIGhpcyBuYXR1cmUgc2VlbWVkLA=="
            + "U28gZGFyaW5nIGFuZCBzd2VldCBoaXMgdGhvdWdodC4="
            + "VGhpcyBvdGhlciBtYW4gSSBoYWQgZHJlYW1lZA==";
}
