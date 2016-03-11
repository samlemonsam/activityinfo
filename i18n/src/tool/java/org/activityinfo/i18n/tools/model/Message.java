package org.activityinfo.i18n.tools.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;

import java.util.List;

/**
 * A translatable message with placeholders
 */
public class Message {

    public static final char SINGLE_QUOTE = '\'';


    public static class Chunk {
        private int argumentIndex;
        private String format;
        private String text;

        public Chunk(int argumentIndex, String format) {
            this.argumentIndex = argumentIndex;
            this.format = format;
        }

        public Chunk(String text) {
            this.argumentIndex = -1;
            this.text = text;
        }

        public int getArgumentIndex() {
            return argumentIndex;
        }

        public String getFormat() {
            return format;
        }

        public String getText() {
            return text;
        }

        public boolean isPlaceholder() {
            return argumentIndex >= 0;
        }


        @Override
        public String toString() {
            if(isPlaceholder()) {
                return "{" + argumentIndex + "}";
            } else {
                return "[" + text + "]";
            }
        }
    }

    private List<Chunk> chunks = Lists.newArrayList();

    public Message(String pattern) throws MessageFormatException {
        PeekingIterator<Character> it = Iterators.peekingIterator(Lists.charactersOf(pattern).iterator());

        while(it.hasNext()) {
            if(it.peek() == '{') {
                chunks.add(consumePlaceholder(it));
            } else {
                chunks.add(consumeStaticChunk(it));
            }
        }
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    private Chunk consumePlaceholder(PeekingIterator<Character> it) throws MessageFormatException {
        char openingBrace = it.next();
        Preconditions.checkState(openingBrace == '{');

        int argumentIndex = consumeIndex(it);

        StringBuilder format = new StringBuilder();
        while(it.hasNext()) {
            char c = it.next();
            if(c == '}') {
                break;
            } else {
                format.append(c);
            }
        }
        return new Chunk(argumentIndex, format.toString());
    }

    private int consumeIndex(PeekingIterator<Character> it) throws MessageFormatException {
        StringBuilder number = new StringBuilder();
        while(Character.isDigit(it.peek())) {
            number.append(it.next());
        }
        if(number.length() == 0) {
            throw new MessageFormatException("Expected number at placeholder, found: " + it.peek());
        }
        try {
            return Integer.parseInt(number.toString());
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Invalid placeholder number: '" + number.toString() + "'");
        }
    }

    private Chunk consumeStaticChunk(PeekingIterator<Character> it) {
        StringBuilder text = new StringBuilder();
        boolean quoted = false;
        while(it.hasNext()) {
            if(!quoted && it.peek() == '{') {
                break;
            }
            char c = it.next();
            if(c == SINGLE_QUOTE) {
                if (it.hasNext() && it.peek() == SINGLE_QUOTE) {
                    text.append(it.next());
                } else {
                    quoted = !quoted;
                }
            } else {
                text.append(c);
            }
        }
        return new Chunk(text.toString());
    }


    public boolean hasPlaceholder(int argumentIndex) {
        for(Chunk chunk : chunks) {
            if(chunk.isPlaceholder() && chunk.getArgumentIndex() == argumentIndex) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return chunks.toString();
    }
}
