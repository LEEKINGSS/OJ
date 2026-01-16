/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_long_ } from '../models/BaseResponse_long_';
import type { BaseResponse_QuestionDetailSafeVo_ } from '../models/BaseResponse_QuestionDetailSafeVo_';
import type { BaseResponse_QuestionListSafeVo_ } from '../models/BaseResponse_QuestionListSafeVo_';
import type { BaseResponse_string_ } from '../models/BaseResponse_string_';
import type { QuestionAddRequest } from '../models/QuestionAddRequest';
import type { QuestionUpdateRequest } from '../models/QuestionUpdateRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class QuestionControllerService {
    /**
     * addQuestion
     * @param questionAddRequest questionAddRequest
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addQuestionUsingPost(
        questionAddRequest: QuestionAddRequest,
    ): CancelablePromise<BaseResponse_long_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/add',
            body: questionAddRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * deleteQuestion
     * @param id id
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static deleteQuestionUsingPost(
        id?: number,
    ): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/delete',
            query: {
                'id': id,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * getQuestionById
     * @param id id
     * @returns BaseResponse_QuestionDetailSafeVo_ OK
     * @throws ApiError
     */
    public static getQuestionByIdUsingGet(
        id: number,
    ): CancelablePromise<BaseResponse_QuestionDetailSafeVo_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/question/detail/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * listQuestion
     * @param name name
     * @param page page
     * @param size size
     * @param tags tags
     * @returns BaseResponse_QuestionListSafeVo_ OK
     * @throws ApiError
     */
    public static listQuestionUsingGet(
        name?: string,
        page: number = 1,
        size: number = 10,
        tags?: string,
    ): CancelablePromise<BaseResponse_QuestionListSafeVo_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/question/list',
            query: {
                'name': name,
                'page': page,
                'size': size,
                'tags': tags,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * parseWordDocument
     * @param file
     * @returns BaseResponse_string_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static parseWordDocumentUsingPost(
        file?: Blob,
    ): CancelablePromise<BaseResponse_string_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/parseWord',
            formData: {
                'file': file,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * updateQuestion
     * @param questionupdateRequest questionupdateRequest
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static updateQuestionUsingPost(
        questionupdateRequest: QuestionUpdateRequest,
    ): CancelablePromise<BaseResponse_long_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/update',
            body: questionupdateRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
}
