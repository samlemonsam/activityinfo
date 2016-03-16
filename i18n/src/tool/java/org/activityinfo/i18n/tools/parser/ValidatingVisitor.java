package org.activityinfo.i18n.tools.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.activityinfo.i18n.tools.model.Message;
import org.activityinfo.i18n.tools.model.MessageFormatException;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.output.MessageDecorator;

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

        Message format;
        try {
            format = new Message(decorator.apply(inputMessage));
        } catch (MessageFormatException e) {
            System.err.println(String.format("Invalid message format %s[%s]: %s", key, input.getLanguage(), e.getMessage()));
            return false;
        }
        // make sure there are enough arguments for all the placeholders
        for (Message.Chunk chunk : format.getChunks()) {
            if(chunk.isPlaceholder()) {
                boolean validPlaceholder = true;
                if(chunk.getArgumentIndex() >= decl.getParameters().size()) {
                    System.err.println(String.format("Invalid translation %s[%s]: not enough arguments for [%s]",
                            key, input.getLanguage(), inputMessage));
                    validPlaceholder = false;
                }
                
                if(!validatePlaceholder(chunk)) {
                    validPlaceholder = false;
                }
                
                if(!validPlaceholder) {
                    return false;
                }
            }
        }


        // And make sure all arguments are used...
        if(decl.getParameters() != null) {
            for (int i = 0; i < decl.getParameters().size(); ++i) {
                if (!format.hasPlaceholder(i)) {
                    System.err.println(String.format("Invalid translation %s[%s]: argument %d is not used: [%s]",
                            key, input.getLanguage(), i, inputMessage));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validatePlaceholder(Message.Chunk chunk) {
        String parts[] = chunk.getFormat().split(",");
        
        // TODO
        return true;
    }
}
