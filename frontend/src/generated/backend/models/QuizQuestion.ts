/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ImageWithMask } from './ImageWithMask';
import type { MultipleChoicesQuestion } from './MultipleChoicesQuestion';
export type QuizQuestion = {
    id: number;
    checkSpell?: boolean;
    imageWithMask?: ImageWithMask;
    multipleChoicesQuestion: MultipleChoicesQuestion;
};

