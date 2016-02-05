package org.xbib.time.chronic;

import org.xbib.time.chronic.tags.Tag;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Token {
    private String word;
    private List<Tag<?>> tags;

    public Token(String word) {
        this.word = word;
        tags = new LinkedList<>();
    }

    public String getWord() {
        return word;
    }

    /**
     * Tag this token with the specified tag
     */
    public void tag(Tag<?> newTag) {
        tags.add(newTag);
    }

    /**
     * Remove all tags of the given class
     */
    public void untag(Class<?> tagClass) {
        Iterator<Tag<?>> tagIter = tags.iterator();
        while (tagIter.hasNext()) {
            Tag<?> tag = tagIter.next();
            if (tagClass.isInstance(tag)) {
                tagIter.remove();
            }
        }
    }

    /**
     * Return true if this token has any tags
     */
    public boolean isTagged() {
        return !tags.isEmpty();
    }

    /**
     * Return the Tag that matches the given class
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> T getTag(Class<T> tagClass) {
        List<T> matches = getTags(tagClass);
        T matchingTag = null;
        if (matches.size() > 0) {
            matchingTag = matches.get(0);
        }
        return matchingTag;
    }

    public List<Tag<?>> getTags() {
        return tags;
    }

    /**
     * Return the Tag that matches the given class
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag<?>> List<T> getTags(Class<T> tagClass) {
        List<T> matches = new LinkedList<>();
        for (Tag<?> tag : tags) {
            if (tagClass.isInstance(tag)) {
                matches.add((T) tag);
            }
        }
        return matches;
    }

    @Override
    public String toString() {
        return word + " " + tags;
    }
}
