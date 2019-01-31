package com.freezinghipster.youtubealgodeceiver;

public class DeceiverRunnerOptions {
    private long maxPlayTime;
    private boolean shuttingDownIfQueueIsEmpty;
    private long queueCycleTime;

    public DeceiverRunnerOptions() {
        this.maxPlayTime = 15000;
        this.shuttingDownIfQueueIsEmpty = true;
        this.queueCycleTime = 10000;
    }

    public long getQueueCycleTime() {
        return queueCycleTime;
    }

    public void setQueueCycleTime(long queueCycleTime) {
        this.queueCycleTime = queueCycleTime;
    }

    public long getMaxPlayTime() {
        return maxPlayTime;
    }

    public void setMaxPlayTime(long maxPlayTime) {
        this.maxPlayTime = maxPlayTime;
    }

    public boolean isShuttingDownIfQueueIsEmpty() {
        return shuttingDownIfQueueIsEmpty;
    }

    public void setShuttingDownIfQueueIsEmpty(boolean shuttingDownIfQueueIsEmpty) {
        this.shuttingDownIfQueueIsEmpty = shuttingDownIfQueueIsEmpty;
    }

    @Override
    public String toString() {
        return "DeceiverRunnerOptions{" +
                "maxPlayTime=" + maxPlayTime +
                ", shuttingDownIfQueueIsEmpty=" + shuttingDownIfQueueIsEmpty +
                ", queueCycleTime=" + queueCycleTime +
                '}';
    }
}
