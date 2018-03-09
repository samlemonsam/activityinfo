/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.i18n.tools.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.i18n.server.MessageFormatUtils;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.output.MessageDecorator;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class ValidatingVisitor extends VoidVisitorAdapter<Void> {

    private MessageDecorator decorator = new MessageDecorator();

    private final TranslationSet input;
    private TranslationSet validatedSet;

    public ValidatingVisitor(TranslationSet translationSet) {
        this.input = translationSet;
        this.validatedSet = new TranslationSet(translationSet.getLanguage());
    }

    public void setDecorator(MessageDecorator decorator) {
        this.decorator = decorator;
    }

    public TranslationSet getValidatedSet() {
        return validatedSet;
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        String key = AstEvaluator.parseTermKey(n);
        if(input.has(key)) {
            String inputMessage = input.get(key);
            if(validateMessage(key, n, inputMessage)) {
                validatedSet.add(key, input.get(key));
            }
        }

        super.visit(n, arg);
    }

    private boolean validateMessage(String key, MethodDeclaration decl, String inputMessage) {

        List<MessageFormatUtils.TemplateChunk> chunks;
        try {
            chunks = MessageFormatUtils.MessageStyle.MESSAGE_FORMAT.parse(decorator.apply(inputMessage));
        } catch (Exception e) {
            System.err.println(String.format("Invalid message format %s[%s]: %s", key, input.getLanguage(), e.getMessage()));
            return false;
        }

        List<MessageFormatUtils.ArgumentChunk> argumentChunks =
                Lists.newArrayList(Iterables.filter(chunks, MessageFormatUtils.ArgumentChunk.class));

        // Check that each {placeholder} in the translated string has a corresponding
        // argument in the message method.
        for (MessageFormatUtils.ArgumentChunk argumentChunk : argumentChunks) {
            if (!validateFormat(argumentChunk)) {
                System.err.println(String.format("Invalid format string %s[%s]: [%s]",
                        key, input.getLanguage(), inputMessage));
                return false;
            }
            if(argumentChunk.getArgumentNumber() >= decl.getParameters().size()) {
                System.err.println(String.format("Invalid translation %s[%s]: not enough arguments for [%s]",
                        key, input.getLanguage(), inputMessage));
                return false;
            }
        }

        // Now check that every method argument is present in the translated string
        Set<Integer> argumentsUsed = Sets.newHashSet();
        for (MessageFormatUtils.ArgumentChunk argumentChunk : argumentChunks) {
            argumentsUsed.add(argumentChunk.getArgumentNumber());
        }
        if(decl.getParameters() != null) {
            for (int i = 0; i < decl.getParameters().size(); ++i) {
                if (!argumentsUsed.contains(i)) {
                    System.err.println(String.format("Invalid translation %s[%s]: placeholder for argument %d is not present" +
                                    "in translated message: [%s]",
                            key, input.getLanguage(), i, inputMessage));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateFormat(MessageFormatUtils.ArgumentChunk argumentChunk) {
        if(argumentChunk.getFormat() == null) {
            return true;
        }
        switch (argumentChunk.getFormat()) {
            case "date":
            case "localdate":
                return validateDateFormat(argumentChunk.getSubFormat());

            case "number":
                return true;

            default:
                return false;
        }

    }

    private boolean validateDateFormat(String subFormat) {
        return true;
    }
}
