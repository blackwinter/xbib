package com.google.auto.common;

import com.google.common.base.Equivalence;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import java.util.List;

/**
 * A utility class for working with {@link AnnotationValue} instances.
 */
final class AnnotationValues {
    private static final Equivalence<AnnotationValue> ANNOTATION_VALUE_EQUIVALENCE =
            new Equivalence<AnnotationValue>() {
                @Override
                protected boolean doEquivalent(AnnotationValue left, AnnotationValue right) {
                    return left.accept(new SimpleAnnotationValueVisitor6<Boolean, AnnotationValue>() {
                        // LHS is not an annotation or array of annotation values, so just test equality.
                        @Override
                        protected Boolean defaultAction(Object left, AnnotationValue right) {
                            return left.equals(right.accept(
                                    new SimpleAnnotationValueVisitor6<Object, Void>() {
                                        @Override
                                        protected Object defaultAction(Object object, Void unused) {
                                            return object;
                                        }
                                    }, null));
                        }

                        // LHS is an annotation mirror so test equivalence for RHS annotation mirrors
                        // and false for other types.
                        @Override
                        public Boolean visitAnnotation(AnnotationMirror left, AnnotationValue right) {
                            return right.accept(
                                    new SimpleAnnotationValueVisitor6<Boolean, AnnotationMirror>() {
                                        @Override
                                        protected Boolean defaultAction(Object right, AnnotationMirror left) {
                                            return false; // Not an annotation mirror, so can't be equal to such.
                                        }

                                        @Override
                                        public Boolean visitAnnotation(AnnotationMirror right, AnnotationMirror left) {
                                            return AnnotationMirrors.equivalence().equivalent(left, right);
                                        }
                                    }, left);
                        }

                        // LHS is a list of annotation values have to collect-test equivalences, or false
                        // for any other types.
                        @Override
                        public Boolean visitArray(List<? extends AnnotationValue> left, AnnotationValue right) {
                            return right.accept(
                                    new SimpleAnnotationValueVisitor6<Boolean, List<? extends AnnotationValue>>() {
                                        @Override
                                        protected Boolean defaultAction(
                                                Object ignored, List<? extends AnnotationValue> alsoIgnored) {
                                            return false; // Not an annotation mirror, so can't be equal to such.
                                        }

                                        @SuppressWarnings("unchecked") // safe covariant cast
                                        @Override
                                        public Boolean visitArray(
                                                List<? extends AnnotationValue> right,
                                                List<? extends AnnotationValue> left) {
                                            return AnnotationValues.equivalence().pairwise().equivalent(
                                                    (List<AnnotationValue>) left, (List<AnnotationValue>) right);
                                        }
                                    }, left);
                        }

                        @Override
                        public Boolean visitType(TypeMirror left, AnnotationValue right) {
                            return right.accept(
                                    new SimpleAnnotationValueVisitor6<Boolean, TypeMirror>() {
                                        @Override
                                        protected Boolean defaultAction(
                                                Object ignored, TypeMirror alsoIgnored) {
                                            return false; // Not an annotation mirror, so can't be equal to such.
                                        }

                                        @Override
                                        public Boolean visitType(TypeMirror right, TypeMirror left) {
                                            return MoreTypes.equivalence().equivalent(left, right);
                                        }
                                    }, left);
                        }
                    }, right);
                }

                @Override
                protected int doHash(AnnotationValue value) {
                    return value.accept(new SimpleAnnotationValueVisitor6<Integer, Void>() {
                        @Override
                        public Integer visitAnnotation(AnnotationMirror value, Void ignore) {
                            return AnnotationMirrors.equivalence().hash(value);
                        }

                        @SuppressWarnings("unchecked") // safe covariant cast
                        @Override
                        public Integer visitArray(
                                List<? extends AnnotationValue> values, Void ignore) {
                            return AnnotationValues.equivalence().pairwise().hash((List<AnnotationValue>) values);
                        }

                        @Override
                        public Integer visitType(TypeMirror value, Void ignore) {
                            return MoreTypes.equivalence().hash(value);
                        }

                        @Override
                        protected Integer defaultAction(Object value, Void ignored) {
                            return value.hashCode();
                        }
                    }, null);
                }
            };

    private AnnotationValues() {
    }

    /**
     * Returns an {@link Equivalence} for {@link AnnotationValue} as annotation values may
     * contain {@link AnnotationMirror} instances some of whose implementations delegate
     * equality tests to {@link Object#equals} whereas the documentation explicitly states
     * that instance/reference equality is not the proper test.
     */
    public static Equivalence<AnnotationValue> equivalence() {
        return ANNOTATION_VALUE_EQUIVALENCE;
    }
}
