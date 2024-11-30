/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MemoryTracker } from '../models/MemoryTracker';
import type { SelfEvaluation } from '../models/SelfEvaluation';
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';
export class RestMemoryTrackerControllerService {
    constructor(public readonly httpRequest: BaseHttpRequest) {}
    /**
     * @param memoryTracker
     * @param requestBody
     * @returns MemoryTracker OK
     * @throws ApiError
     */
    public selfEvaluate(
        memoryTracker: number,
        requestBody: SelfEvaluation,
    ): CancelablePromise<MemoryTracker> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/memory-trackers/{memoryTracker}/self-evaluate',
            path: {
                'memoryTracker': memoryTracker,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param memoryTracker
     * @returns MemoryTracker OK
     * @throws ApiError
     */
    public removeFromRepeating(
        memoryTracker: number,
    ): CancelablePromise<MemoryTracker> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/memory-trackers/{memoryTracker}/remove',
            path: {
                'memoryTracker': memoryTracker,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param memoryTracker
     * @param successful
     * @returns MemoryTracker OK
     * @throws ApiError
     */
    public markAsRepeated(
        memoryTracker: number,
        successful: boolean,
    ): CancelablePromise<MemoryTracker> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/memory-trackers/{memoryTracker}/mark-as-repeated',
            path: {
                'memoryTracker': memoryTracker,
            },
            query: {
                'successful': successful,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param memoryTracker
     * @returns MemoryTracker OK
     * @throws ApiError
     */
    public show1(
        memoryTracker: number,
    ): CancelablePromise<MemoryTracker> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/memory-trackers/{memoryTracker}',
            path: {
                'memoryTracker': memoryTracker,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @returns MemoryTracker OK
     * @throws ApiError
     */
    public getRecentlyReviewedPoints(): CancelablePromise<Array<MemoryTracker>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/memory-trackers/recently-reviewed',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @returns MemoryTracker OK
     * @throws ApiError
     */
    public getRecentReviewPoints(): CancelablePromise<Array<MemoryTracker>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/memory-trackers/recent',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
}
