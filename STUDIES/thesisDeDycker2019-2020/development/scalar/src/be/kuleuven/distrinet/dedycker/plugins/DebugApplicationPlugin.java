package be.kuleuven.distrinet.dedycker.plugins;

import be.kuleuven.distrinet.dedycker.requests.ApplicationRequest;
import be.kuleuven.distrinet.scalar.core.Plugin;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.reporting.Report;
import be.kuleuven.distrinet.scalar.requests.Request;

import java.util.ArrayList;

public class DebugApplicationPlugin extends Plugin {

    private boolean active = false;

    @Override
    protected void onRequest(Request r) {
        Report rep = new Report("debug-request");
        rep.put("id", r.id());
        rep.put("request", r.data().type());
        rep.put("duration", r.data().endTime() - r.data().beginTime());
        rep.put("result", r.data().result().toString());
        rep.put("relevant", r.statisticallyRelevant());
        rep.sendToConsole();
        if (r instanceof ApplicationRequest)
            rep.put("DATA:", ((ApplicationRequest) r).messageOnFailure());
        rep.sendToConsole();
    }

    @Override
    protected void onStartUp() {

    }

    @Override
    protected void onWarmUp() {

    }

    @Override
    protected void onRampUp() {

    }

    @Override
    protected void onPeak() {

    }

    @Override
    protected void onRampDown() {

    }

    @Override
    protected void onCoolDown() {

    }

    @Override
    protected void onStop(ArrayList<Request> arrayList) {

    }

    @Override
    protected void onTermination() {

    }

    @Override
    protected void onInitialization() {
        try {
            this.active = data().getAsBoolean("dump_detailed_message");
        } catch (DataException dataException) {
        }
    }
}
