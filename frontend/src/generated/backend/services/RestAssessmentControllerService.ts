/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AssessmentResult } from '../models/AssessmentResult';
import type { QuestionAnswerPair } from '../models/QuestionAnswerPair';
import type { QuizQuestion } from '../models/QuizQuestion';
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';
export class RestAssessmentControllerService {
    constructor(public readonly httpRequest: BaseHttpRequest) {}
    /**
     * @param notebook
     * @param requestBody
     * @returns AssessmentResult OK
     * @throws ApiError
     */
    public submitAssessmentResult(
        notebook: number,
        requestBody: Array<QuestionAnswerPair>,
    ): CancelablePromise<AssessmentResult> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/assessment/{notebook}',
            path: {
                'notebook': notebook,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param notebook
     * @returns QuizQuestion OK
     * @throws ApiError
     */
    public generateAssessmentQuestions(
        notebook: number,
    ): CancelablePromise<Array<QuizQuestion>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/assessment/questions/{notebook}',
            path: {
                'notebook': notebook,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
}
