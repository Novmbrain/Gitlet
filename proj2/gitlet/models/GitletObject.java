package gitlet.models;

import java.io.Serializable;

/**
 * @className: GitletObject
 * @author: Wenjie FU
 * @date: 27/01/2024
 **/
public abstract class GitletObject implements Serializable {
    protected abstract String sha1Hash();
}
