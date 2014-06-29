package org.reactor;

import static org.reactor.request.parser.AbstractReactorRequestDataParser.forDataType;
import static org.reactor.response.NoResponse.NO_RESPONSE;
import com.google.common.collect.Lists;
import org.reactor.discovery.ReactorTopologyDiscoveringVisitor;
import org.reactor.request.ReactorRequest;
import org.reactor.request.ReactorRequestParsingException;
import org.reactor.request.parser.AbstractReactorRequestDataParser;
import org.reactor.request.parser.ReactorRequestParameterDefinition;
import org.reactor.response.CommandHelpResponse;
import org.reactor.response.ReactorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReactor<T> implements Reactor {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractReactor.class);
    private final AbstractReactorRequestDataParser<T> dataParser;

    public AbstractReactor(Class<T> requestDataType) {
        this.dataParser = forDataType(requestDataType);
    }

    @Override
    public void accept(ReactorTopologyDiscoveringVisitor topologyVisitor) {
        dataParser.accept(topologyVisitor);
    }

    @Override
    public final ReactorResponse react(String sender, String reactorInput) {
        try {
            return react(dataParser.parseRequestWithData(sender, getTriggeringExpression(), reactorInput));
        } catch (ReactorRequestParsingException e) {
            LOG.error("An error occurred while parsing Request", e);
            // TODO handle passing list of possible parameters into response object
            return new CommandHelpResponse(e.getMessage(), this, Lists.<ReactorRequestParameterDefinition>newArrayList());
        }
    }

    private ReactorResponse react(ReactorRequest<T> reactorRequest) {
        if (!reactorRequest.triggerMatches(getTriggeringExpression())) {
            LOG.warn("Trigger does not match triggering expression: {} != {}", reactorRequest.getTrigger(),
                getTriggeringExpression());
            return NO_RESPONSE;
        }
        return doReact(reactorRequest);

    }

    protected abstract ReactorResponse doReact(ReactorRequest<T> reactorRequest);
}
