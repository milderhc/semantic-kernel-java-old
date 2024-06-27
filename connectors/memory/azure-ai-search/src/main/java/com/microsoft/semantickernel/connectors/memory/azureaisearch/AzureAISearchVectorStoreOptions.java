// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.azureaisearch;

import com.azure.search.documents.SearchDocument;
import com.microsoft.semantickernel.memory.VectorStoreRecordMapper;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;

/**
 * Options for an Azure AI Search vector store.
 *
 * @param <Record> the record type
 */
public class AzureAISearchVectorStoreOptions<Record> {
    private final String defaultCollectionName;
    private final Class<Record> recordClass;
    private final VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;

    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    public String getDefaultCollectionName() {
        return defaultCollectionName;
    }

    public Class<Record> getRecordClass() {
        return recordClass;
    }

    public VectorStoreRecordDefinition getRecordDefinition() {
        return recordDefinition;
    }

    public VectorStoreRecordMapper<Record, SearchDocument> getVectorStoreRecordMapper() {
        return vectorStoreRecordMapper;
    }

    private AzureAISearchVectorStoreOptions(
        String defaultCollectionName,
        Class<Record> recordClass,
        VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper,
        VectorStoreRecordDefinition recordDefinition) {
        this.defaultCollectionName = defaultCollectionName;
        this.recordClass = recordClass;
        this.vectorStoreRecordMapper = vectorStoreRecordMapper;
        this.recordDefinition = recordDefinition;
    }

    public static class Builder<Record> {
        private String defaultCollectionName;
        private VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper;
        private Class<Record> recordClass;
        private VectorStoreRecordDefinition recordDefinition;

        public Builder<Record> withRecordClass(Class<Record> recordClass) {
            this.recordClass = recordClass;
            return this;
        }

        /**
         * Sets the default collection name.
         *
         * @param defaultCollectionName the default collection name
         * @return the builder
         */
        public Builder<Record> withDefaultCollectionName(String defaultCollectionName) {
            this.defaultCollectionName = defaultCollectionName;
            return this;
        }

        public Builder<Record> withVectorStoreRecordMapper(
            VectorStoreRecordMapper<Record, SearchDocument> vectorStoreRecordMapper) {
            this.vectorStoreRecordMapper = vectorStoreRecordMapper;
            return this;
        }

        public Builder<Record> withRecordDefinition(VectorStoreRecordDefinition recordDefinition) {
            this.recordDefinition = recordDefinition;
            return this;
        }

        public AzureAISearchVectorStoreOptions<Record> build() {
            if (recordClass == null) {
                throw new IllegalArgumentException("recordClass must be provided");
            }

            return new AzureAISearchVectorStoreOptions<>(defaultCollectionName,
                recordClass,
                vectorStoreRecordMapper,
                recordDefinition);
        }
    }
}
