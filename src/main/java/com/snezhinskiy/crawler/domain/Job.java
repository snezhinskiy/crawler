package com.snezhinskiy.crawler.domain;

import com.snezhinskiy.crawler.domain.embedded.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="job")
public class Job {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "job_id_seq_gen")
    @GenericGenerator(
        name = "job_id_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "job_id_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "config_id")
    protected JobConfig config;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    protected LocalDateTime createdAt;

    @Column(name = "started_at")
    protected LocalDateTime startedAt;

    @Column(name = "finished_at")
    protected LocalDateTime finishedAt;

    protected Integer uploadCounter;

    protected Integer documentCounter;

    @Override
    public String toString() {
        return "Job{" +
            "id=" + id +
            ", config=" + (config != null ? config.getId() : null) +
            ", status=" + status +
            ", createdAt=" + createdAt +
            ", startedAt=" + startedAt +
            ", finishedAt=" + finishedAt +
            ", uploadCounter=" + uploadCounter +
            ", documentCounter=" + documentCounter +
            '}';
    }
}
