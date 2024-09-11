package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.Checkpoint;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisSaverTest {

    @Inject
    RedisSaver redisSaver;

    @Test
    @Order(1)
    public void testInsertCheckpoint() {

        Map<String, Object> state = new HashMap<>();
        state.put("1", "1");

        Checkpoint checkpoint = Checkpoint.builder()
            .id("1")
            .nextNodeId("b")
            .nodeId("a")
            .state(state)
            .build();

        final RunnableConfig runnableConfig = RunnableConfig.builder().threadId("11").build();

        RunnableConfig newRunnableConfig = redisSaver.put(runnableConfig, checkpoint);
        assertThat(newRunnableConfig.checkPointId()).isPresent().contains("1");

    }

    @Test
    @Order(2)
    public void testListCheckpoints() {
        final RunnableConfig runnableConfig = RunnableConfig.builder().threadId("11").build();
        final Collection<Checkpoint> checkpoints = redisSaver.list(runnableConfig);
        assertThat(checkpoints)
            .hasSize(1);
    }

    @Test
    @Order(3)
    public void insertANewCheckpointToSameThread() {
        Map<String, Object> state = new HashMap<>();
        state.put("1", "2");

        Checkpoint checkpoint = Checkpoint.builder()
            .id("2")
            .nextNodeId("c")
            .nodeId("b")
            .state(state)
            .build();

        final RunnableConfig runnableConfig = RunnableConfig.builder().threadId("11").build();

        RunnableConfig newRunnableConfig = redisSaver.put(runnableConfig, checkpoint);
        assertThat(newRunnableConfig.checkPointId()).isPresent().contains("2");
    }

    @Test
    @Order(4)
    public void getLastCheckpoint() {
        final RunnableConfig runnableConfig = RunnableConfig.builder().threadId("11").build();
        final Optional<Checkpoint> checkpoint = redisSaver.get(runnableConfig);
        assertThat(checkpoint).isPresent();

        final Checkpoint c = checkpoint.get();
        assertThat(c.getId()).isEqualTo("2");
        assertThat(c.getNextNodeId()).isEqualTo("c");
        assertThat(c.getNodeId()).isEqualTo("b");
        assertThat(c.getState()).extracting("1").isEqualTo("2");

    }

    @Test
    @Order(5)
    public void overrideCheckpoint() {

        final RunnableConfig runnableConfig = RunnableConfig.builder()
            .threadId("11")
            .checkPointId("1")
            .build();

        Map<String, Object> state = new HashMap<>();
        state.put("1", "3");

        Checkpoint checkpoint = Checkpoint.builder()
            .id("1")
            .nextNodeId("c")
            .nodeId("b")
            .state(state)
            .build();

        final RunnableConfig config = redisSaver.put(runnableConfig, checkpoint);
        assertThat(config.checkPointId()).contains("1");
    }

    @Test
    @Order(6)
    public void getConcreteCheckpoint() {
        final RunnableConfig runnableConfig = RunnableConfig.builder()
            .threadId("11")
            .checkPointId("1")
            .build();

        final Optional<Checkpoint> checkpoint = redisSaver.get(runnableConfig);

        assertThat(checkpoint).isPresent();

        final Checkpoint c = checkpoint.get();
        assertThat(c.getState()).extracting("1").isEqualTo("3");

    }

}
