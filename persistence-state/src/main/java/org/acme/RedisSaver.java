package org.acme;

import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;

import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.Checkpoint;

@ApplicationScoped
public class RedisSaver implements BaseCheckpointSaver {

    final ListCommands<String, Checkpoint> listCommands;
    final KeyCommands<String> keyCommands;

    public RedisSaver( RedisDataSource redisDataSource) {
        listCommands = redisDataSource.list(Checkpoint.class);
        keyCommands = redisDataSource.key();
    }

    @Override
    public Collection<Checkpoint> list(RunnableConfig config) {

        final String threadId = config.threadId().orElse("");
        return listCommands.lrange(threadId, 0, -1);
    }

    @Override
    public Optional<Checkpoint> get(RunnableConfig config) {

        final String threadId = config.threadId().orElse("");

        if (config.checkPointId().isPresent()) {
            final List<Checkpoint> checkpoints = listCommands.lrange(threadId, 0, -1);

            return config.checkPointId()
                .flatMap( id -> checkpoints.stream()
                    .filter( checkpoint -> checkpoint.getId().equals(id) )
                    .findFirst());
        }

        return Optional.ofNullable(listCommands.lindex(threadId, 0));
    }

    @Override
    public RunnableConfig put(RunnableConfig config, Checkpoint checkpoint) {

        final String threadId = config.threadId().orElseThrow(
            () -> new IllegalArgumentException("ThreadId should be provided to save the checkpoint")
        );

        if (config.checkPointId().isPresent()) { // Replace Checkpoint
            final List<Checkpoint> checkpoints = listCommands.lrange(threadId, 0, -1);
            String checkPointId = config.checkPointId().get();

            int index = IntStream.range(0, checkpoints.size())
                .filter(i -> checkpoints.get(i).getId().equals(checkPointId))
                .findFirst()
                .orElseThrow(() -> (new NoSuchElementException(String.format("Checkpoint with id %s not found!", checkPointId))));
            checkpoints.set(index, checkpoint );

            keyCommands.del(threadId);
            listCommands.lpush(threadId, checkpoints.toArray(new Checkpoint[0]));

            return config;

        }

        listCommands.lpush(threadId, checkpoint);

        return RunnableConfig.builder(config)
            .checkPointId(checkpoint.getId())
            .build();
    }
}
