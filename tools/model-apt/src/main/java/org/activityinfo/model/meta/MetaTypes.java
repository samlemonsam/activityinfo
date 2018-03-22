package org.activityinfo.model.meta;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.AbstractTypeVisitor8;

public class MetaTypes {
    private MetaTypes() {}

    public static MetaType of(TypeMirror type) {
      return type.accept(new AbstractTypeVisitor8<MetaType, Void>() {
          @Override
          public MetaType visitIntersection(IntersectionType t, Void aVoid) {
              throw new UnsupportedOperationException("visitIntersection");
          }

          @Override
          public MetaType visitUnion(UnionType t, Void aVoid) {
              throw new UnsupportedOperationException("visitUnion");
          }

          @Override
          public MetaType visitPrimitive(PrimitiveType t, Void aVoid) {
              return new PrimitiveMetaType(t);
          }

          @Override
          public MetaType visitNull(NullType t, Void aVoid) {
              throw new UnsupportedOperationException("visitNull");
          }

          @Override
          public MetaType visitArray(ArrayType t, Void aVoid) {
              throw new UnsupportedOperationException("visitArray");
          }

          @Override
          public MetaType visitDeclared(DeclaredType t, Void aVoid) {
              TypeElement element = (TypeElement) t.asElement();
              if(element.getQualifiedName().contentEquals("java.lang.String")) {
                  return new StringMetaType(element);
              }
              if(element.getQualifiedName().contentEquals("java.util.Set")) {
                  return new SetMetaType(t, t.getTypeArguments().get(0));
              }
              if(element.getQualifiedName().contentEquals("org.activityinfo.model.resource.ResourceId")) {
                  return new ResourceIdType(element);
              }
              if(element.getKind() == ElementKind.ENUM) {
                  return new EnumMetaType(element);
              }
              throw new UnsupportedOperationException("visitDeclared: " + element.getQualifiedName());
          }

          @Override
          public MetaType visitError(ErrorType t, Void aVoid) {
              throw new UnsupportedOperationException("visitError");
          }

          @Override
          public MetaType visitTypeVariable(TypeVariable t, Void aVoid) {
              throw new UnsupportedOperationException("visitTypeVariable: " + t.getUpperBound());
          }

          @Override
          public MetaType visitWildcard(WildcardType t, Void aVoid) {
              throw new UnsupportedOperationException("visitWildcard");
          }

          @Override
          public MetaType visitExecutable(ExecutableType t, Void aVoid) {
              throw new UnsupportedOperationException("visitExecutable");
          }

          @Override
          public MetaType visitNoType(NoType t, Void aVoid) {
              throw new UnsupportedOperationException("visitNoType");
          }
      }, null);
    }
}
