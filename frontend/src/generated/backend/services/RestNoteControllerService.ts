/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AudioUploadDTO } from '../models/AudioUploadDTO';
import type { NoteAccessoriesDTO } from '../models/NoteAccessoriesDTO';
import type { NoteAccessory } from '../models/NoteAccessory';
import type { NoteCreationDTO } from '../models/NoteCreationDTO';
import type { NoteCreationRresult } from '../models/NoteCreationRresult';
import type { NoteInfo } from '../models/NoteInfo';
import type { NoteRealm } from '../models/NoteRealm';
import type { NoteTopic } from '../models/NoteTopic';
import type { RedirectToNoteResponse } from '../models/RedirectToNoteResponse';
import type { ReviewSetting } from '../models/ReviewSetting';
import type { SearchTerm } from '../models/SearchTerm';
import type { WikidataAssociationCreation } from '../models/WikidataAssociationCreation';
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';
export class RestNoteControllerService {
    constructor(public readonly httpRequest: BaseHttpRequest) {}
    /**
     * @param parentNote
     * @param requestBody
     * @returns NoteCreationRresult OK
     * @throws ApiError
     */
    public createNote(
        parentNote: number,
        requestBody: NoteCreationDTO,
    ): CancelablePromise<NoteCreationRresult> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/{parentNote}/create',
            path: {
                'parentNote': parentNote,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @param requestBody
     * @returns NoteRealm OK
     * @throws ApiError
     */
    public updateWikidataId(
        note: number,
        requestBody: WikidataAssociationCreation,
    ): CancelablePromise<NoteRealm> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/{note}/updateWikidataId',
            path: {
                'note': note,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @param requestBody
     * @returns NoteTopic OK
     * @throws ApiError
     */
    public searchForLinkTargetWithin(
        note: number,
        requestBody: SearchTerm,
    ): CancelablePromise<Array<NoteTopic>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/{note}/search',
            path: {
                'note': note,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @param requestBody
     * @returns RedirectToNoteResponse OK
     * @throws ApiError
     */
    public updateReviewSetting(
        note: number,
        requestBody: ReviewSetting,
    ): CancelablePromise<RedirectToNoteResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/{note}/review-setting',
            path: {
                'note': note,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @returns NoteRealm OK
     * @throws ApiError
     */
    public deleteNote(
        note: number,
    ): CancelablePromise<Array<NoteRealm>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/{note}/delete',
            path: {
                'note': note,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param requestBody
     * @returns NoteTopic OK
     * @throws ApiError
     */
    public searchForLinkTarget(
        requestBody: SearchTerm,
    ): CancelablePromise<Array<NoteTopic>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/search',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @param targetNote
     * @param asFirstChild
     * @returns NoteRealm OK
     * @throws ApiError
     */
    public moveAfter(
        note: number,
        targetNote: number,
        asFirstChild: string,
    ): CancelablePromise<Array<NoteRealm>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/notes/move_after/{note}/{targetNote}/{asFirstChild}',
            path: {
                'note': note,
                'targetNote': targetNote,
                'asFirstChild': asFirstChild,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @returns NoteRealm OK
     * @throws ApiError
     */
    public show1(
        note: number,
    ): CancelablePromise<NoteRealm> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/notes/{note}',
            path: {
                'note': note,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @param formData
     * @returns NoteAccessory OK
     * @throws ApiError
     */
    public updateNoteAccessories(
        note: number,
        formData?: NoteAccessoriesDTO,
    ): CancelablePromise<NoteAccessory> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/notes/{note}',
            path: {
                'note': note,
            },
            formData: formData,
            mediaType: 'multipart/form-data',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @returns NoteRealm OK
     * @throws ApiError
     */
    public undoDeleteNote(
        note: number,
    ): CancelablePromise<NoteRealm> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/notes/{note}/undo-delete',
            path: {
                'note': note,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @param formData
     * @returns NoteAccessory OK
     * @throws ApiError
     */
    public uploadAudio(
        note: number,
        formData?: AudioUploadDTO,
    ): CancelablePromise<NoteAccessory> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/notes/{note}/audio',
            path: {
                'note': note,
            },
            formData: formData,
            mediaType: 'multipart/form-data',
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @returns NoteInfo OK
     * @throws ApiError
     */
    public getNoteInfo(
        note: number,
    ): CancelablePromise<NoteInfo> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/notes/{note}/note-info',
            path: {
                'note': note,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
    /**
     * @param note
     * @returns NoteAccessory OK
     * @throws ApiError
     */
    public showNoteAccessory(
        note: number,
    ): CancelablePromise<NoteAccessory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/notes/{note}/accessory',
            path: {
                'note': note,
            },
            errors: {
                500: `Internal Server Error`,
            },
        });
    }
}
